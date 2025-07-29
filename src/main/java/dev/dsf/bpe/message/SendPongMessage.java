package dev.dsf.bpe.message;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.mail.AggregateErrorMailService;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.SendTaskErrorConverter;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedBytesGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedDurationMillisGenerator;
import dev.dsf.bpe.util.task.input.generator.ErrorInputComponentGenerator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class SendPongMessage extends AbstractTaskMessageSend
{
	private final AggregateErrorMailService errorMailService;

	public SendPongMessage(ProcessPluginApi api, AggregateErrorMailService errorMailService)
	{
		super(api);

		this.errorMailService = errorMailService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(errorMailService, "errorMailService");
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution,
			Variables variables)
	{
		List<ProcessError> errorList = ErrorListUtils.getErrorMessageList(execution);
		long downloadResourceSizeBytes = variables.getLong(ExecutionVariables.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue());
		if (downloadResourceSizeBytes >= 0)
		{
			Long downloadedBytes = variables.getLong(ExecutionVariables.DOWNLOADED_BYTES.getValue());
			Long downloadedDurationMillis = variables.getLong(ExecutionVariables.DOWNLOADED_DURATION_MILLIS.getValue());
			String downloadResourceReference = variables
					.getString(ExecutionVariables.DOWNLOAD_RESOURCE_REFERENCE.getValue());

			Stream<Task.ParameterComponent> downloadedBytesParameter = downloadedBytes != null
					? Stream.of(DownloadedBytesGenerator.create(downloadedBytes))
					: Stream.empty();
			Stream<Task.ParameterComponent> downloadedDurationMillisParameter = downloadedDurationMillis != null
					? Stream.of(DownloadedDurationMillisGenerator.create(downloadedDurationMillis))
					: Stream.empty();
			Stream<Task.ParameterComponent> downloadedResourceReferenceParameter = downloadResourceReference != null
					? Stream.of(DownloadResourceReferenceGenerator.create(downloadResourceReference))
					: Stream.empty();

			return Stream.of(downloadedBytesParameter, downloadedDurationMillisParameter,
					downloadedResourceReferenceParameter, ErrorInputComponentGenerator.create(errorList).stream())
					.flatMap(stream -> stream);
		}
		else
		{
			return ErrorInputComponentGenerator.create(errorList).stream();
		}
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws Exception
	{
		Target target = variables.getTarget();
		Task mainTask = variables.getStartTask();
		PingStatusGenerator.updatePongStatusOutput(mainTask, target);
		variables.updateTask(mainTask);
		super.doExecute(execution, variables);
	}

	@Override
	protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
			String errorMessage)
	{
		PingPongLogger logger = new PingPongLogger(SendPongMessage.class, variables.getStartTask());
		Target target = variables.getTarget();
		Task startTask = variables.getStartTask();

		ProcessError error = SendTaskErrorConverter.convert(exception,
				"Sending pong message to " + target.getEndpointUrl());

		ErrorListUtils.add(error, execution);
		PingStatusGenerator.updatePongStatusOutput(startTask, ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR);
		variables.setString(ExecutionVariables.STATUS_CODE.getValue(),
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR);
		variables.updateTask(startTask);

		logger.info("Request to {} resulted in error: {}", target.getEndpointUrl(), error.message());
	}
}
