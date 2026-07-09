package dev.dsf.bpe.service.ping;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Targets;
import dev.dsf.bpe.v2.variables.Variables;

public class StoreResults implements ServiceTask, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(StoreResults.class);
	private final AggregateErrorMailService errorMailService;
	private final CodeSystem.DsfPingUnits.Code networkSpeedUnit;
	private final PingStatusGenerator pingStatusGenerator;

	public StoreResults(AggregateErrorMailService errorMailService, CodeSystem.DsfPingUnits.Code networkSpeedUnit,
			PingStatusGenerator pingStatusGenerator)
	{
		this.networkSpeedUnit = networkSpeedUnit;
		this.errorMailService = errorMailService;
		this.pingStatusGenerator = pingStatusGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(errorMailService, "errorMailService");
	}

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Storing results for process started with Task {}",
				variables.getStartTask().getIdElement().getValue());
		Task task = variables.getStartTask();
		Targets targets = variables.getTargets();
		Map<Target, List<ProcessError>> errorsPerTarget = new HashMap<>();
		String resourceVersion = api.getProcessPluginDefinition().getResourceVersion();

		List<ProcessError> localProcessErrors = ErrorListUtils.getErrorList(variables).getEntries();

		ProcessError.toTaskOutput(localProcessErrors, resourceVersion).forEach(task::addOutput);

		targets.getEntries().stream().sorted(Comparator.comparing(Target::getEndpointIdentifierValue)).forEach(target ->
		{
			String correlationKey = target.getCorrelationKey();

			ProcessErrors errors = ErrorListUtils.getErrorList(variables, correlationKey);
			CodeSystem.DsfPingStatus.Code statusCode = (CodeSystem.DsfPingStatus.Code) variables
					.getVariable(ExecutionVariables.statusCode.correlatedValue(correlationKey));
			long downloadResourceSizeBytes = variables.getLong(ExecutionVariables.downloadResourceSizeBytes.name());
			if (downloadResourceSizeBytes >= 0) // if fat-ping
			{
				Long downloadedBytes = variables
						.getLong(ExecutionVariables.downloadedBytes.correlatedValue(correlationKey));
				Duration downloadedDuration = (Duration) variables
						.getVariable(ExecutionVariables.downloadedDuration.correlatedValue(correlationKey));

				Optional<CodeSystem.DsfPingUnits.Code.SpeedAndUnit> downloadSpeedAndUnit = calculateNetworkSpeed(
						downloadedBytes, downloadedDuration);
				BigDecimal downloadSpeed = downloadSpeedAndUnit.map(CodeSystem.DsfPingUnits.Code.SpeedAndUnit::speed)
						.orElse(null);
				CodeSystem.DsfPingUnits.Code downloadSpeedUnit = downloadSpeedAndUnit
						.map(CodeSystem.DsfPingUnits.Code.SpeedAndUnit::unit).orElse(null);

				Long uploadedBytes = variables
						.getLong(ExecutionVariables.uploadedBytes.correlatedValue(correlationKey));
				Duration uploadedDurationMillis = (Duration) variables
						.getVariable(ExecutionVariables.uploadedDuration.correlatedValue(correlationKey));

				Optional<CodeSystem.DsfPingUnits.Code.SpeedAndUnit> uploadSpeedAndUnit = calculateNetworkSpeed(
						uploadedBytes, uploadedDurationMillis);
				BigDecimal uploadSpeed = uploadSpeedAndUnit.map(CodeSystem.DsfPingUnits.Code.SpeedAndUnit::speed)
						.orElse(null);
				CodeSystem.DsfPingUnits.Code uploadSpeedUnit = uploadSpeedAndUnit
						.map(CodeSystem.DsfPingUnits.Code.SpeedAndUnit::unit).orElse(null);


				pingStatusGenerator.createPingStatusOutput(target, statusCode, errors.getEntries(), downloadSpeed,
						downloadSpeedUnit, uploadSpeed, uploadSpeedUnit).ifPresent(task::addOutput);
			}
			else // if slim-ping
			{
				pingStatusGenerator.createPingStatusOutput(target, statusCode, errors.getEntries())
						.ifPresent(task::addOutput);
			}
			errorsPerTarget.put(target, errors.getEntries());
		});

		variables.updateTask(task);

		errorMailService.send(task.getIdElement(), localProcessErrors, errorsPerTarget);

		logger.debug("Successfully stored results for task {}", variables.getStartTask().getIdElement().getValue());
	}

	private Optional<CodeSystem.DsfPingUnits.Code.SpeedAndUnit> calculateNetworkSpeed(Long transferredBytes,
			Duration transferDuration)
	{
		if (transferredBytes != null && transferDuration != null)
		{
			if (Objects.isNull(networkSpeedUnit))
			{
				return Optional.of(
						CodeSystem.DsfPingUnits.Code.calculateSpeedWithFittingUnit(transferredBytes, transferDuration));
			}
			else
			{
				return Optional.of(new CodeSystem.DsfPingUnits.Code.SpeedAndUnit(
						networkSpeedUnit.calculateSpeed(transferredBytes, transferDuration), networkSpeedUnit));
			}
		}
		else
		{
			return Optional.empty();
		}
	}
}
