package dev.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceSizeGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.StatusType;

public class SendPing extends AbstractTaskMessageSend
{
	private static final Logger logger = LoggerFactory.getLogger(SendPing.class);

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
	protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
			String errorMessage)
	{
		Target target = variables.getTarget();

		String statusCode = exception instanceof WebApplicationException w && w.getResponse() != null
				&& w.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()
						? ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED
						: ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE;

		execution.setVariableLocal(ConstantsPing.getBpmnExecutionVariableStatusCode(), statusCode);
		String specialErrorMessage = createErrorMessage(exception);
		execution.setVariableLocal(ConstantsPing.getBpmnExecutionVariableErrorMessage(), specialErrorMessage);
		logger.info("Request to {} resulted in status {}", target.getEndpointUrl(), statusCode);
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
			return "Error when sending ping message: " + statusInfo.getStatusCode() + " "
					+ statusInfo.getReasonPhrase();
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
