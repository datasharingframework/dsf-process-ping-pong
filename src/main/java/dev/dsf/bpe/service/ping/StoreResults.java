package dev.dsf.bpe.service.ping;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.ProcessErrors;
import dev.dsf.bpe.mail.AggregateErrorMailService;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreResults extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(StoreResults.class);
	private final AggregateErrorMailService errorMailService;
	private CodeSystem.DsfPingUnits.Code networkSpeedUnit;

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
		logger.debug("Storing results for process started with Task {}",
				variables.getStartTask().getIdElement().getValue());
		Task task = variables.getStartTask();
		Targets targets = variables.getTargets();
		Map<Target, List<ProcessError>> errorsPerTarget = new HashMap<>();

		ProcessError.toTaskOutput(ErrorListUtils.getErrorList(execution).getEntries()).forEach(task::addOutput);

		targets.getEntries().stream().sorted(Comparator.comparing(Target::getEndpointIdentifierValue)).forEach(target ->
		{
			String correlationKey = target.getCorrelationKey();

			ProcessErrors errors = ErrorListUtils.getErrorList(execution, correlationKey);
			CodeSystem.DsfPingStatus.Code statusCode = (CodeSystem.DsfPingStatus.Code) variables
					.getVariable(ExecutionVariables.statusCode.correlatedValue(correlationKey));
			long downloadResourceSizeBytes = variables.getLong(ExecutionVariables.downloadResourceSizeBytes.name());
			if (downloadResourceSizeBytes >= 0) // if fat-ping
			{
				Long downloadedBytes = variables
						.getLong(ExecutionVariables.downloadedBytes.correlatedValue(correlationKey));
				Duration downloadedDuration = (Duration) variables
						.getVariable(ExecutionVariables.downloadedDuration.correlatedValue(correlationKey));

				BigDecimal downloadSpeed;
				if (Objects.isNull(networkSpeedUnit))
				{
					CodeSystem.DsfPingUnits.Code.SpeedAndUnit speedAndUnit = CodeSystem.DsfPingUnits.Code
							.calculateSpeedWithFittingUnit(downloadedBytes, downloadedDuration);
					downloadSpeed = speedAndUnit.speed();
					networkSpeedUnit = speedAndUnit.unit();
				}
				else
				{
					downloadSpeed = downloadedBytes != null && downloadedDuration != null
							? networkSpeedUnit.calculateSpeed(downloadedBytes, downloadedDuration)
							: null;
				}

				Long uploadedBytes = variables
						.getLong(ExecutionVariables.uploadedBytes.correlatedValue(correlationKey));
				Duration uploadedDurationMillis = (Duration) variables
						.getVariable(ExecutionVariables.uploadedDuration.correlatedValue(correlationKey));

				BigDecimal uploadSpeed;
				if (Objects.isNull(networkSpeedUnit))
				{
					CodeSystem.DsfPingUnits.Code.SpeedAndUnit speedAndUnit = CodeSystem.DsfPingUnits.Code
							.calculateSpeedWithFittingUnit(uploadedBytes, uploadedDurationMillis);
					uploadSpeed = speedAndUnit.speed();
					networkSpeedUnit = speedAndUnit.unit();
				}
				else
				{
					uploadSpeed = uploadedBytes != null && uploadedDurationMillis != null
							? networkSpeedUnit.calculateSpeed(uploadedBytes, uploadedDurationMillis)
							: null;
				}

				task.addOutput(PingStatusGenerator.createPingStatusOutput(target, statusCode, errors.getEntries(),
						downloadSpeed, uploadSpeed, networkSpeedUnit));
			}
			else // if slim-ping
			{
				task.addOutput(PingStatusGenerator.createPingStatusOutput(target, statusCode, errors.getEntries()));
			}
			errorsPerTarget.put(target, errors.getEntries());
		});

		variables.updateTask(task);

		errorMailService.send(task.getIdElement(), errorsPerTarget);

		logger.debug("Successfully stored results for task {}", variables.getStartTask().getIdElement().getValue());
	}
}
