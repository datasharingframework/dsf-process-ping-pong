package dev.dsf.bpe.service.ping;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.ProcessErrors;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class SavePong implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SavePong.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Target target = variables.getTarget();
		logger.debug("Pong received from {}. Saving pong information...", target.getEndpointUrl());
		String correlationKey = target.getCorrelationKey();

		Task pong = variables.getLatestTask();

		Optional<org.hl7.fhir.r4.model.Duration> optDownloadedDuration = api.getTaskHelper()
				.getFirstInputParameterValue(pong, CodeSystem.DsfPing.URL,
						CodeSystem.DsfPing.Code.DOWNLOADED_DURATION_MILLIS.getValue(),
						org.hl7.fhir.r4.model.Duration.class);
		optDownloadedDuration.ifPresent(duration -> variables.setJsonVariable(
				ExecutionVariables.uploadedDuration.correlatedValue(correlationKey),
				Duration.ofMillis(duration.getValue().longValue())));

		Optional<DecimalType> optDownloadedBytes = api.getTaskHelper().getFirstInputParameterValue(pong,
				CodeSystem.DsfPing.URL, CodeSystem.DsfPing.Code.DOWNLOADED_BYTES.getValue(), DecimalType.class);
		optDownloadedBytes.ifPresent(decimalType -> variables.setLong(
				ExecutionVariables.uploadedBytes.correlatedValue(correlationKey), decimalType.getValue().longValue()));


		ProcessErrors errorList = new ProcessErrors(parseInputs(pong));

		ErrorListUtils.addAll(errorList, variables, correlationKey);
		variables.setJsonVariable(ExecutionVariables.statusCode.correlatedValue(correlationKey),
				CodeSystem.DsfPingStatus.Code.PONG_RECEIVED);

		logger.debug("Saved pong information.");
	}

	private List<ProcessError> parseInputs(Task task)
	{
		List<Task.ParameterComponent> inputs = task.getInput().stream().filter(
				input -> CodeSystem.DsfPing.Code.ERROR.getValue().equals(input.getType().getCodingFirstRep().getCode()))
				.toList();

		return inputs.stream()
				.map(input -> ProcessError.toError(
						input.getExtensionByUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR),
						ConstantsPing.PROCESS_NAME_PONG))
				.toList();
	}
}
