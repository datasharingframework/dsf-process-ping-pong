package dev.dsf.bpe.message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.PingProcessPluginDefinition;
import dev.dsf.bpe.util.task.SendTaskErrorConverter;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceSizeGenerator;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.MessageSendTask;
import dev.dsf.bpe.v2.activity.task.DefaultTaskSender;
import dev.dsf.bpe.v2.activity.task.TaskSender;
import dev.dsf.bpe.v2.activity.values.SendTaskValues;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.error.MessageSendTaskErrorHandler;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class SendPingMessage implements MessageSendTask
{
	private static final Logger logger = LoggerFactory.getLogger(SendPingMessage.class);

	@Override
	public List<Task.ParameterComponent> getAdditionalInputParameters(ProcessPluginApi api, Variables variables,
			SendTaskValues sendTaskValues, Target target)
	{
		String downloadResourceReference = variables.getString(ExecutionVariables.downloadResourceReference.name());
		long downloadResourceSizeBytes = variables.getLong(ExecutionVariables.downloadResourceSizeBytes.name());

		List<Task.ParameterComponent> additionalInputParameters = new ArrayList<>();

		String resourceVersion = api.getProcessPluginDefinition().getResourceVersion();

		if (downloadResourceReference != null)
			additionalInputParameters
					.add(DownloadResourceReferenceGenerator.create(downloadResourceReference, resourceVersion));

		additionalInputParameters.add(DownloadResourceSizeGenerator.create(downloadResourceSizeBytes, resourceVersion));

		Task.ParameterComponent endpointIdentifierComponent = api.getTaskHelper().createInput(
				new Reference().setIdentifier(getLocalEndpointIdentifier(api)).setType(ResourceType.Endpoint.name()),
				CodeSystem.DsfPing.URL, CodeSystem.DsfPing.Code.ENDPOINT_IDENTIFIER.getValue(), resourceVersion);
		endpointIdentifierComponent.getType().getCodingFirstRep().setVersion(resourceVersion);

		additionalInputParameters.add(endpointIdentifierComponent);

		return additionalInputParameters;
	}

	@Override
	public TaskSender getTaskSender(ProcessPluginApi api, Variables variables, SendTaskValues sendTaskValues)
	{
		return new DefaultTaskSender(api, variables, sendTaskValues, getBusinessKeyStrategy())
		{
			@Override
			protected IdType doSend(Task task, String targetEndpointUrl)
			{
				IdType taskId = super.doSend(task, targetEndpointUrl);
				if (taskId != null)
				{
					variables.setStringLocal(ExecutionVariables.pingTaskId.name(), taskId.getIdPart());
				}
				return taskId;
			}
		};
	}

	@Override
	public MessageSendTaskErrorHandler getErrorHandler()
	{
		return new MessageSendTaskErrorHandler()
		{
			@Override
			public ErrorBoundaryEvent handleErrorBoundaryEvent(ProcessPluginApi processPluginApi, Variables variables,
					ErrorBoundaryEvent errorBoundaryEvent)
			{
				return errorBoundaryEvent;
			}

			@Override
			public Exception handleException(ProcessPluginApi processPluginApi, Variables variables,
					SendTaskValues sendTaskValues, Exception e)
			{
				Target target = variables.getTarget();
				SendTaskErrorConverter.ProcessErrorWithStatusCode errorAndStatus = SendTaskErrorConverter
						.convertLocal(e, true, ConstantsPing.PROCESS_NAME_PING);

				variables.setJsonVariableLocal(ExecutionVariables.error.name(), errorAndStatus.error());
				variables.setJsonVariableLocal(ExecutionVariables.statusCode.name(), errorAndStatus.statusCode());

				logger.info("Request to {} resulted in error: {}", target.getEndpointUrl(),
						errorAndStatus.error().concept().getDisplay());

				return null;
			}
		};
	}

	private Identifier getLocalEndpointIdentifier(ProcessPluginApi api)
	{
		return api.getEndpointProvider().getLocalEndpointIdentifier()
				.orElseThrow(() -> new IllegalStateException("Local endpoint identifier unknown"));
	}
}
