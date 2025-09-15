package dev.dsf.bpe.message;

import java.time.Duration;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.task.SendTaskErrorConverter;
import dev.dsf.bpe.util.task.input.generator.DownloadedBytesGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedDurationGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.process_error.ProcessErrorValueImpl;

public class CleanupPongMessage extends AbstractTaskMessageSend
{
	private static final Logger logger = LoggerFactory.getLogger(CleanupPongMessage.class);

	public CleanupPongMessage(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution,
			Variables variables)
	{
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();
		Long downloadedBytes = variables.getLong(ExecutionVariables.downloadedBytes.correlatedValue(correlationKey));
		Duration downloadedDuration = (Duration) variables
				.getVariable(ExecutionVariables.downloadedDuration.correlatedValue(correlationKey));

		Stream<Task.ParameterComponent> downloadedBytesParameter = downloadedBytes != null
				? Stream.of(DownloadedBytesGenerator.create(downloadedBytes))
				: Stream.empty();
		Stream<Task.ParameterComponent> downloadedDurationParameter = downloadedDuration != null
				? Stream.of(DownloadedDurationGenerator.create(downloadedDuration))
				: Stream.empty();

		return Stream.of(downloadedBytesParameter, downloadedDurationParameter).flatMap(s -> s);
	}

	@Override
	protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
			String errorMessage)
	{
		Target target = variables.getTarget();
		SendTaskErrorConverter.ProcessErrorWithStatusCode errorAndStatus = SendTaskErrorConverter
				.convertLocal(exception, false, ConstantsPing.PROCESS_NAME_PING);

		execution.setVariableLocal(ExecutionVariables.error.name(), new ProcessErrorValueImpl(errorAndStatus.error()));
		execution.setVariableLocal(ExecutionVariables.statusCode.name(), CodeSystem.DsfPing.Code.ERROR.getValue());

		logger.info("Request to {} resulted in error: {}", target.getEndpointUrl(),
				errorAndStatus.error().concept().getDisplay());
	}

	@Override
	protected void sendTask(DelegateExecution execution, Variables variables, Target target,
			String instantiatesCanonical, String messageName, String businessKey, String profile,
			Stream<Task.ParameterComponent> additionalInputParameters)
	{
		Target newTarget = new Target()
		{
			@Override
			public String getOrganizationIdentifierValue()
			{
				return target.getOrganizationIdentifierValue();
			}

			@Override
			public String getEndpointIdentifierValue()
			{
				return target.getEndpointIdentifierValue();
			}

			@Override
			public String getEndpointUrl()
			{
				return target.getEndpointUrl();
			}

			@Override
			public String getCorrelationKey()
			{
				return null;
			}
		};

		super.sendTask(execution, variables, newTarget, instantiatesCanonical, messageName, businessKey, profile,
				additionalInputParameters);
	}
}
