package dev.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.SendTaskErrorConverter;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceSizeGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.process_error.ProcessErrorValueImpl;
import dev.dsf.fhir.client.FhirWebserviceClient;

public class SendPingMessage extends AbstractTaskMessageSend
{
	private IdType taskId;

	public SendPingMessage(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables)
	{
		String downloadResourceReference = variables
				.getString(ExecutionVariables.DOWNLOAD_RESOURCE_REFERENCE.getValue());
		long downloadResourceSizeBytes = variables.getLong(ExecutionVariables.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue());

		Stream<ParameterComponent> downloadResourceReferenceStream = downloadResourceReference == null ? Stream.empty()
				: Stream.of(DownloadResourceReferenceGenerator.create(downloadResourceReference));
		Stream<ParameterComponent> downloadResourceSizeBytesStream = Stream
				.of(DownloadResourceSizeGenerator.create(downloadResourceSizeBytes));
		Stream<ParameterComponent> endpointIdentifierStream = Stream.of(api.getTaskHelper().createInput(
				new Reference().setIdentifier(getLocalEndpointIdentifier()).setType(ResourceType.Endpoint.name()),
				CodeSystem.DsfPing.URL, CodeSystem.DsfPing.Code.ENDPOINT_IDENTIFIER.getValue()));

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
			execution.setVariableLocal(ExecutionVariables.PING_TASK_ID.getValue(), taskId.getIdPart());
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
		PingPongLogger logger = new PingPongLogger(SendPingMessage.class, variables.getStartTask());
		Target target = variables.getTarget();
		ProcessError error = SendTaskErrorConverter.convert(exception,
				"Sending ping message to " + target.getEndpointUrl());

		execution.setVariableLocal(ExecutionVariables.ERROR.getValue(), new ProcessErrorValueImpl(error));
		execution.setVariableLocal(ExecutionVariables.STATUS_CODE.getValue(), CodeSystem.DsfPing.Code.ERROR.getValue());

		logger.info("Request to {} resulted in error: {}", target.getEndpointUrl(), error.message());
	}

	@Override
	protected void addErrorMessage(Task task, String errorMessage)
	{
		// error message part of status extension
	}

	private Identifier getLocalEndpointIdentifier()
	{
		return api.getEndpointProvider().getLocalEndpointIdentifier()
				.orElseThrow(() -> new IllegalStateException("Local endpoint identifier unknown"));
	}
}
