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
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.SendTaskErrorConverter;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceSizeGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
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
		PingPongLogger logger = new PingPongLogger(SendPingMessage.class, variables.getStartTask());
		Target target = variables.getTarget();
		ProcessError error = SendTaskErrorConverter.convert(exception,
				"Sending ping message to " + target.getEndpointUrl());

		try
		{
			execution.setVariableLocal(ConstantsPing.getBpmnExecutionVariableError(), ProcessError.toString(error));
			execution.setVariableLocal(ConstantsPing.getBpmnExecutionVariableStatusCode(),
					ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR);
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}

		logger.info("Request to {} resulted in error: {}", target.getEndpointUrl(), error.message());
	}

	@Override
	protected void addErrorMessage(Task task, String errorMessage)
	{
		// error message part of
	}

	private Identifier getLocalEndpointIdentifier()
	{
		return api.getEndpointProvider().getLocalEndpointIdentifier()
				.orElseThrow(() -> new IllegalStateException("Local endpoint identifier unknown"));
	}
}
