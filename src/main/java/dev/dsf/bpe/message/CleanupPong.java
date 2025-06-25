package dev.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.task.input.generator.DownloadedBytesGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedDurationMillisGenerator;
import dev.dsf.bpe.util.task.input.generator.NetworkSpeedMetricGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class CleanupPong extends AbstractTaskMessageSend
{
	private static final Logger logger = LoggerFactory.getLogger(CleanupPong.class);
	public CleanupPong(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution,
			Variables variables)
	{
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();
		Integer downloadedBytes = variables
				.getInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes(correlationKey));
		Long downloadedDurationMillis = variables
				.getLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(correlationKey));

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
		Target target = variables.getTarget();

		String responseCode = exception instanceof WebApplicationException w && w.getResponse() != null
				? Response.Status.fromStatusCode(w.getResponse().getStatus()).toString()
				: "unknown";
		String statusCode = ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR;

		execution.setVariableLocal(ConstantsPing.getBpmnExecutionVariableStatusCode(), statusCode);
		String specialErrorMessage = createErrorMessage(exception);
		ProcessError pingSendError = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CLEANUP_PONG,
				"Sending cleanup message to " + target.getEndpointUrl(), ConstantsPing.POTENTIAL_FIX_URL_DUMMY,
				specialErrorMessage);
		try
		{
			execution.setVariableLocal(ConstantsPing.getBpmnExecutionVariableError(),
					ProcessError.toString(pingSendError));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
		logger.info("Request to {} resulted in status {}", target.getEndpointUrl(), responseCode);
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

	private String createErrorMessage(Exception exception)
	{
		if (exception instanceof WebApplicationException w
				&& (exception.getMessage() == null || exception.getMessage().isBlank()))
		{
			Response.StatusType statusInfo = w.getResponse().getStatusInfo();
			return statusInfo.getStatusCode() + " " + statusInfo.getReasonPhrase();
		}
		else
			return exception.getMessage();
	}
}
