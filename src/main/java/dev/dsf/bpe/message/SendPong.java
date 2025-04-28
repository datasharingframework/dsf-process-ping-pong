package dev.dsf.bpe.message;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.mail.AggregateErrorMailService;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedBytesGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedDurationMillisGenerator;
import dev.dsf.bpe.util.task.input.generator.ErrorInputComponentGenerator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.StatusType;

public class SendPong extends AbstractTaskMessageSend
{
	private final AggregateErrorMailService errorMailService;

	public SendPong(ProcessPluginApi api, AggregateErrorMailService errorMailService)
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
		int downloadResourceSizeBytes = variables
				.getInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES);
		if (downloadResourceSizeBytes >= 0)
		{
			Integer downloadedBytes = variables.getInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes());
			Long downloadedDurationMillis = variables
					.getLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis());
			String downloadResourceReference = variables
					.getString(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_REFERENCE);

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
		PingPongLogger logger = new PingPongLogger(SendPong.class, variables.getStartTask());
		Target target = variables.getTarget();
		Task startTask = variables.getStartTask();

		String statusCode = exception instanceof WebApplicationException w && w.getResponse() != null
				? Response.Status.fromStatusCode(w.getResponse().getStatus()).toString()
				: "unknown";

		String specialErrorMessage = createErrorMessage(exception);
		ProcessError pongSendError = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PONG,
				ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_PONG,
				"Sending pong message to " + target.getEndpointUrl(), ConstantsPing.POTENTIAL_FIX_URL_DUMMY,
				specialErrorMessage);
		ErrorListUtils.add(pongSendError, execution);
		PingStatusGenerator.updatePongStatusOutput(startTask, ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR);
		variables.setString(ConstantsPing.getBpmnExecutionVariableStatusCode(),
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR);

		logger.info("Request to {} resulted in status {}", target.getEndpointUrl(), statusCode);
		variables.updateTask(startTask);
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
}
