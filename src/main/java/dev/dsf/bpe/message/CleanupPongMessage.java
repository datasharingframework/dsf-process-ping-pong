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
import dev.dsf.bpe.util.task.SendTaskErrorConverter;
import dev.dsf.bpe.util.task.input.generator.DownloadedBytesGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedDurationGenerator;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.MessageSendTask;
import dev.dsf.bpe.v2.activity.task.DefaultTaskSender;
import dev.dsf.bpe.v2.activity.task.TaskSender;
import dev.dsf.bpe.v2.activity.values.SendTaskValues;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.error.MessageSendTaskErrorHandler;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class CleanupPongMessage implements MessageSendTask
{
	private static final Logger logger = LoggerFactory.getLogger(CleanupPongMessage.class);

	@Override
	public List<Task.ParameterComponent> getAdditionalInputParameters(ProcessPluginApi api, Variables variables,
			SendTaskValues sendTaskValues, Target target)
	{
		String correlationKey = target.getCorrelationKey();
		Long downloadedBytes = variables.getLong(ExecutionVariables.downloadedBytes.correlatedValue(correlationKey));
		Duration downloadedDuration = variables
				.getVariable(ExecutionVariables.downloadedDuration.correlatedValue(correlationKey));

		List<Task.ParameterComponent> additionalInputParameters = new ArrayList<>();

		String resourceVersion = api.getProcessPluginDefinition().getResourceVersion();

		if (downloadedBytes != null)
			additionalInputParameters.add(DownloadedBytesGenerator.create(downloadedBytes, resourceVersion));

		if (downloadedDuration != null)
			additionalInputParameters.add(DownloadedDurationGenerator.create(downloadedDuration, resourceVersion));

		return additionalInputParameters;
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
						.convertLocal(e, false, ConstantsPing.PROCESS_NAME_PING);

				variables.setJsonVariableLocal(ExecutionVariables.error.name(), errorAndStatus.error());
				variables.setStringLocal(ExecutionVariables.statusCode.name(),
						CodeSystem.DsfPing.Code.ERROR.getValue());

				logger.info("Request to {} resulted in error: {}", target.getEndpointUrl(),
						errorAndStatus.error().concept().getDisplay());

				return null;
			}
		};
	}

	@Override
	public TaskSender getTaskSender(ProcessPluginApi api, Variables variables, SendTaskValues sendTaskValues)
	{
		return new DefaultTaskSender(api, variables, sendTaskValues, this.getBusinessKeyStrategy())
		{
			@Override
			protected Target getTarget()
			{
				Target oldTarget = super.getTarget();
				return new Target()
				{
					@Override
					public String getOrganizationIdentifierValue()
					{
						return oldTarget.getOrganizationIdentifierValue();
					}

					@Override
					public String getEndpointIdentifierValue()
					{
						return oldTarget.getEndpointIdentifierValue();
					}

					@Override
					public String getEndpointUrl()
					{
						return oldTarget.getEndpointUrl();
					}

					@Override
					public String getCorrelationKey()
					{
						return null;
					}
				};
			}
		};
	}
}
