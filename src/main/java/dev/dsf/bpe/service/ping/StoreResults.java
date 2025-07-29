package dev.dsf.bpe.service.ping;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.ConstantsPing;
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
	private final String networkSpeedUnit;

	public StoreResults(ProcessPluginApi api, AggregateErrorMailService errorMailService, String networkSpeedUnit)
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
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(StoreResults.class, variables.getStartTask());

		logger.debug("Storing results for process started with Task {}",
				variables.getStartTask().getIdElement().getValue());
		Task task = variables.getStartTask();
		Targets targets = variables.getTargets();

		ErrorOutputComponentGenerator.create(ErrorListUtils.getErrorMessageList(execution)).forEach(task::addOutput);

		targets.getEntries().stream().sorted(Comparator.comparing(Target::getEndpointIdentifierValue)).forEach(target ->
		{
			String correlationKey = target.getCorrelationKey();

			List<ProcessError> errors = ErrorListUtils.getErrorMessageList(execution, correlationKey);
			String statusCode = errors.isEmpty() ? ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_COMPLETED
					: ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR;
			long downloadResourceSizeBytes = variables
					.getLong(ExecutionVariables.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue());
			List<ProcessError> errorMessageList = ErrorListUtils.getErrorMessageList(execution, correlationKey);
			if (downloadResourceSizeBytes >= 0) // if fat-ping
			{
				Long downloadedBytes = variables
						.getLong(ExecutionVariables.DOWNLOADED_BYTES.correlatedValue(correlationKey));
				Long downloadedDurationMillis = variables
						.getLong(ExecutionVariables.DOWNLOADED_DURATION_MILLIS.correlatedValue(correlationKey));

				BigDecimal downloadSpeed = downloadedBytes != null && downloadedDurationMillis != null
						? NetworkSpeedCalculator.calculate(downloadedBytes, downloadedDurationMillis, networkSpeedUnit)
						: null;

				Long uploadedBytes = variables
						.getLong(ExecutionVariables.UPLOADED_BYTES.correlatedValue(correlationKey));
				Long uploadedDurationMillis = variables
						.getLong(ExecutionVariables.UPLOADED_DURATION_MILLIS.correlatedValue(correlationKey));

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
			errors.forEach(error -> errorMailService.addError(target, error));
		});

		variables.updateTask(task);

		errorMailService.send(task.getIdElement());

		logger.debug("Successfully stored results for task {}", variables.getStartTask().getIdElement().getValue());
	}
}
