package dev.dsf.bpe.service.ping;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
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
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.duration.DurationValueImpl;

public class SavePong extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(SavePong.class);

	public SavePong(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		Target target = variables.getTarget();
		logger.debug("Pong received from {}. Saving pong information...", target.getEndpointUrl());
		String correlationKey = target.getCorrelationKey();
		delegateExecution.removeVariable("statusCode");

		Task pong = variables.getLatestTask();

		Optional<org.hl7.fhir.r4.model.Duration> optDownloadedDuration = api.getTaskHelper()
				.getFirstInputParameterValue(pong, CodeSystem.DsfPing.URL,
						CodeSystem.DsfPing.Code.DOWNLOADED_DURATION_MILLIS.getValue(),
						org.hl7.fhir.r4.model.Duration.class);
		optDownloadedDuration.ifPresent(
				duration -> variables.setVariable(ExecutionVariables.uploadedDuration.correlatedValue(correlationKey),
						new DurationValueImpl(Duration.ofMillis(duration.getValue().longValue()))));

		Optional<DecimalType> optDownloadedBytes = api.getTaskHelper().getFirstInputParameterValue(pong,
				CodeSystem.DsfPing.URL, CodeSystem.DsfPing.Code.DOWNLOADED_BYTES.getValue(), DecimalType.class);
		optDownloadedBytes.ifPresent(decimalType -> variables.setLong(
				ExecutionVariables.uploadedBytes.correlatedValue(correlationKey), decimalType.getValue().longValue()));


		ProcessErrors errorList = new ProcessErrors(parseInputs(pong));

		ErrorListUtils.addAll(errorList, delegateExecution, correlationKey);

		logger.debug("Saved pong information.");
	}

	private List<ProcessError> parseInputs(Task task)
	{
		List<Task.ParameterComponent> inputs = task.getInput().stream().filter(
				input -> CodeSystem.DsfPing.Code.ERROR.getValue().equals(input.getType().getCodingFirstRep().getCode()))
				.toList();

		return inputs.stream()
				.map(input -> ProcessError
						.toError(input.getExtensionByUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR)))
				.toList();
	}
}
