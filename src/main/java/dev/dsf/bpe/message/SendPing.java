package dev.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceSizeGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.fhir.client.FhirWebserviceClient;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.StatusType;

public class SendPing extends AbstractTaskMessageSend
{
	private static final Logger logger = LoggerFactory.getLogger(SendPing.class);
	private IdType taskId;

	public SendPing(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables)
	{
		String downloadResourceReference = variables
				.getString(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_REFERENCE);
		int downloadResourceSizeBytes = variables
				.getInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES);

		Stream<ParameterComponent> downloadResourceReferenceStream = downloadResourceReference == null ? Stream.empty()
				: Stream.of(DownloadResourceReferenceGenerator.create(downloadResourceReference));
		Stream<ParameterComponent> downloadResourceSizeBytesStream = Stream
				.of(DownloadResourceSizeGenerator.create(downloadResourceSizeBytes));
		Stream<ParameterComponent> endpointIdentifierStream = Stream.of(api.getTaskHelper().createInput(
				new Reference().setIdentifier(getLocalEndpointIdentifier()).setType(ResourceType.Endpoint.name()),
				ConstantsPing.CODESYSTEM_DSF_PING, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_ENDPOINT_IDENTIFIER));

		return Stream.concat(endpointIdentifierStream,
				Stream.concat(downloadResourceReferenceStream, downloadResourceSizeBytesStream));
	}

	@Override
	protected void sendTask(DelegateExecution execution, Variables variables, Target target,
			String instantiatesCanonical, String messageName, String businessKey, String profile,
			Stream<ParameterComponent> additionalInputParameters)
	{
		super.sendTask(execution, variables, target, instantiatesCanonical, messageName, businessKey, profile,
				additionalInputParameters);
		if (taskId != null)
		{
			execution.setVariableLocal(ConstantsPing.BPMN_EXECUTION_VARIABLE_PING_TASK_ID, taskId);
		}
	}

	@Override
	protected IdType doSend(FhirWebserviceClient client, Task task)
	{
		taskId = super.doSend(client, task);
		return taskId;
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
				ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_PING,
				"Sending ping message to " + target.getEndpointUrl(), ConstantsPing.POTENTIAL_FIX_URL_DUMMY,
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
	protected void addErrorMessage(Task task, String errorMessage)
	{
		// error message part of
	}

	private String createErrorMessage(Exception exception)
	{
		if (exception instanceof WebApplicationException w
				&& (exception.getMessage() == null || exception.getMessage().isBlank()))
		{
			StatusType statusInfo = w.getResponse().getStatusInfo();
			return statusInfo.getStatusCode() + " " + statusInfo.getReasonPhrase();
		}
		else
			return exception.getMessage();
	}

	private Identifier getLocalEndpointIdentifier()
	{
		return api.getEndpointProvider().getLocalEndpointIdentifier()
				.orElseThrow(() -> new IllegalStateException("Local endpoint identifier unknown"));
	}
}
