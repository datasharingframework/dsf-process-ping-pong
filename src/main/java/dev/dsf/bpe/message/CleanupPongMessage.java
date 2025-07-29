package dev.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.SendTaskErrorConverter;
import dev.dsf.bpe.util.task.input.generator.DownloadedBytesGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedDurationMillisGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

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
		Long downloadedBytes = variables.getLong(ExecutionVariables.DOWNLOADED_BYTES.correlatedValue(correlationKey));
		Long downloadedDurationMillis = variables
				.getLong(ExecutionVariables.DOWNLOADED_DURATION_MILLIS.correlatedValue(correlationKey));

		Stream<Task.ParameterComponent> downloadedBytesParameter = downloadedBytes != null
				? Stream.of(DownloadedBytesGenerator.create(downloadedBytes))
				: Stream.empty();
		Stream<Task.ParameterComponent> downloadedDurationMillisParameter = downloadedDurationMillis != null
				? Stream.of(DownloadedDurationMillisGenerator.create(downloadedDurationMillis))
				: Stream.empty();

		return Stream.of(downloadedBytesParameter, downloadedDurationMillisParameter).flatMap(s -> s);
	}

	@Override
	protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
			String errorMessage)
	{
		PingPongLogger logger = new PingPongLogger(CleanupPongMessage.class, variables.getStartTask());
		Target target = variables.getTarget();
		ProcessError error = SendTaskErrorConverter.convert(exception,
				"Sending cleanup message to " + target.getEndpointUrl());

		try
		{
			execution.setVariableLocal(ExecutionVariables.ERROR.getValue(), ProcessError.toString(error));
			execution.setVariableLocal(ExecutionVariables.STATUS_CODE.getValue(),
					ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR);
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}

		logger.info("Request to {} resulted in error: {}", target.getEndpointUrl(), error.message());
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
