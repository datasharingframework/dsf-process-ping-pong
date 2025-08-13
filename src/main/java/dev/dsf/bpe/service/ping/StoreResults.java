package dev.dsf.bpe.service.ping;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.mail.AggregateErrorMailService;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.NetworkSpeedCalculator;
import dev.dsf.bpe.util.task.output.generator.ErrorOutputComponentGenerator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreResults extends AbstractServiceDelegate implements InitializingBean
{
	private final AggregateErrorMailService errorMailService;
	private final CodeSystem.DsfPingUnits.Code networkSpeedUnit;

	public StoreResults(ProcessPluginApi api, AggregateErrorMailService errorMailService,
			CodeSystem.DsfPingUnits.Code networkSpeedUnit)
	{
		super(api);
		this.networkSpeedUnit = networkSpeedUnit;
		this.errorMailService = errorMailService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(errorMailService, "errorMailService");
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError
	{
		PingPongLogger logger = new PingPongLogger(StoreResults.class, variables.getStartTask());

		logger.debug("Storing results for process started with Task {}",
				variables.getStartTask().getIdElement().getValue());
		Task task = variables.getStartTask();
		Targets targets = variables.getTargets();
		Map<Target, List<ProcessError>> errorsPerTarget = new HashMap<>();

		ErrorOutputComponentGenerator.create(ErrorListUtils.getErrorMessageList(execution)).forEach(task::addOutput);

		targets.getEntries().stream().sorted(Comparator.comparing(Target::getEndpointIdentifierValue)).forEach(target ->
		{
			String correlationKey = target.getCorrelationKey();

			List<ProcessError> errors = ErrorListUtils.getErrorMessageList(execution, correlationKey);
			CodeSystem.DsfPingStatus.Code statusCode = errors.isEmpty() ? CodeSystem.DsfPingStatus.Code.COMPLETED
					: CodeSystem.DsfPingStatus.Code.ERROR;
			long downloadResourceSizeBytes = variables
					.getLong(ExecutionVariables.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue());
			List<ProcessError> errorMessageList = ErrorListUtils.getErrorMessageList(execution, correlationKey);
			if (downloadResourceSizeBytes >= 0) // if fat-ping
			{
				Long downloadedBytes = variables
						.getLong(ExecutionVariables.DOWNLOADED_BYTES.correlatedValue(correlationKey));
				Duration downloadedDuration = (Duration) variables
						.getVariable(ExecutionVariables.DOWNLOADED_DURATION.correlatedValue(correlationKey));

				BigDecimal downloadSpeed = downloadedBytes != null && downloadedDuration != null
						? NetworkSpeedCalculator.calculate(downloadedBytes, downloadedDuration, networkSpeedUnit)
						: null;

				Long uploadedBytes = variables
						.getLong(ExecutionVariables.UPLOADED_BYTES.correlatedValue(correlationKey));
				Duration uploadedDurationMillis = (Duration) variables
						.getVariable(ExecutionVariables.UPLOADED_DURATION_MILLIS.correlatedValue(correlationKey));

				BigDecimal uploadSpeed = uploadedBytes != null && uploadedDurationMillis != null
						? NetworkSpeedCalculator.calculate(uploadedBytes, uploadedDurationMillis, networkSpeedUnit)
						: null;

				task.addOutput(PingStatusGenerator.createPingStatusOutput(target, statusCode, errorMessageList,
						downloadSpeed, uploadSpeed, networkSpeedUnit));
			}
			else // if slim-ping
			{
				task.addOutput(PingStatusGenerator.createPingStatusOutput(target, statusCode, errorMessageList));
			}
			errorsPerTarget.put(target, errors);
		});

		variables.updateTask(task);

		errorMailService.send(task.getIdElement(), errorsPerTarget);

		logger.debug("Successfully stored results for task {}", variables.getStartTask().getIdElement().getValue());
	}
}
