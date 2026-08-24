package dev.dsf.bpe.message;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessErrors;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.task.SendTaskErrorConverter;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedBytesGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedDurationGenerator;
import dev.dsf.bpe.util.task.input.generator.ErrorInputComponentGenerator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.MessageSendTask;
import dev.dsf.bpe.v2.activity.values.SendTaskValues;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.error.MessageSendTaskErrorHandler;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class SendPongMessage implements MessageSendTask
{
	private static final Logger logger = LoggerFactory.getLogger(SendPongMessage.class);
	private final PingStatusGenerator pingStatusGenerator;
	private boolean includeReference;

	public SendPongMessage(PingStatusGenerator pingStatusGenerator)
	{
		this.pingStatusGenerator = pingStatusGenerator;
	}

	public void setIncludeReference(boolean includeReference)
	{
		this.includeReference = includeReference;
	}

	@Override
	public List<Task.ParameterComponent> getAdditionalInputParameters(ProcessPluginApi api, Variables variables,
			SendTaskValues sendTaskValues, Target target)
	{
		ProcessErrors errorListRemote = ErrorListUtils.getErrorListRemote(variables);
		long downloadResourceSizeBytes = variables.getLong(ExecutionVariables.downloadResourceSizeBytes.name());
		String resourceVersion = api.getProcessPluginDefinition().getResourceVersion();
		if (downloadResourceSizeBytes >= 0)
		{
			Long downloadedBytes = variables.getLong(ExecutionVariables.downloadedBytes.name());
			Duration downloadedDuration = variables.getVariable(ExecutionVariables.downloadedDuration.name());
			String downloadResourceReference = variables.getString(ExecutionVariables.downloadResourceReference.name());

			ArrayList<Task.ParameterComponent> additionalInputParameters = new ArrayList<>();

			if (downloadedBytes != null)
				additionalInputParameters.add(DownloadedBytesGenerator.create(downloadedBytes, resourceVersion));

			if (downloadedDuration != null)
				additionalInputParameters.add(DownloadedDurationGenerator.create(downloadedDuration, resourceVersion));

			if (includeReference && downloadResourceReference != null)
			{
				additionalInputParameters
						.add(DownloadResourceReferenceGenerator.create(downloadResourceReference, resourceVersion));
			}

			additionalInputParameters
					.addAll(ErrorInputComponentGenerator.create(errorListRemote.getEntries(), resourceVersion));

			return additionalInputParameters;
		}
		else
		{
			return ErrorInputComponentGenerator.create(errorListRemote.getEntries(), resourceVersion);
		}
	}

	@Override
	public void execute(ProcessPluginApi api, Variables variables, SendTaskValues sendTaskValues)
			throws ErrorBoundaryEvent, Exception
	{
		Target target = variables.getTarget();
		Task mainTask = variables.getStartTask();
		variables.setJsonVariable(ExecutionVariables.statusCode.name(), CodeSystem.DsfPingStatus.Code.PONG_SENT);
		pingStatusGenerator.updatePongStatusOutput(mainTask, target);
		variables.updateTask(mainTask);
		MessageSendTask.super.execute(api, variables, sendTaskValues);
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
				Task startTask = variables.getStartTask();
				String correlationKey = target.getCorrelationKey();

				SendTaskErrorConverter.ProcessErrorWithStatusCode errorAndStatus = SendTaskErrorConverter
						.convertLocal(e, true, ConstantsPing.PROCESS_NAME_PONG);

				ErrorListUtils.add(errorAndStatus.error(), variables, correlationKey);
				variables.setJsonVariable(ExecutionVariables.statusCode.name(), errorAndStatus.statusCode());
				variables.updateTask(startTask);

				logger.info("Request to {} resulted in error: {}", target.getEndpointUrl(),
						errorAndStatus.error().concept().getDisplay());

				return null;
			}
		};
	}
}
