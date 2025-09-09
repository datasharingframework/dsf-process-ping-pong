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

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.task.SendTaskErrorConverter;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceSizeGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.codesystem.dsfpingstatus.CodeValueImpl;
import dev.dsf.bpe.variables.process_error.ProcessErrorValueImpl;
import dev.dsf.fhir.client.FhirWebserviceClient;

public class SendPingMessage extends AbstractTaskMessageSend
{
	private static final Logger logger = LoggerFactory.getLogger(SendPingMessage.class);
	private IdType taskId;

	public SendPingMessage(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables)
	{
		String downloadResourceReference = variables.getString(ExecutionVariables.downloadResourceReference.name());
		long downloadResourceSizeBytes = variables.getLong(ExecutionVariables.downloadResourceSizeBytes.name());

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
			execution.setVariableLocal(ExecutionVariables.pingTaskId.name(), taskId.getIdPart());
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
		SendTaskErrorConverter.ProcessErrorWithStatusCode errorAndStatus = SendTaskErrorConverter
				.convertLocal(exception, true, ConstantsPing.PROCESS_NAME_PING);

		execution.setVariableLocal(ExecutionVariables.error.name(), new ProcessErrorValueImpl(errorAndStatus.error()));
		execution.setVariableLocal(ExecutionVariables.statusCode.name(),
				new CodeValueImpl(errorAndStatus.statusCode()));

		logger.info("Request to {} resulted in error: {}", target.getEndpointUrl(),
				errorAndStatus.error().concept().getDisplay());
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
