package dev.dsf.bpe.message;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.util.ErrorMessageListUtils;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedBytesGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedDurationMillisGenerator;
import dev.dsf.bpe.util.task.input.generator.ErrorMessageGenerator;
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
	private final ErrorMailService errorMailService;

	public SendPong(ProcessPluginApi api, ErrorMailService errorMailService)
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
		List<String> errorList = ErrorMessageListUtils.getErrorMessageList(execution);
		int downloadResourceSizeBytes = variables
				.getInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES);
		if (downloadResourceSizeBytes >= 0)
		{
			int downloadedBytes = variables.getInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOADED_BYTES);
			long downloadedDurationMillis = variables
					.getLong(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOADED_DURATION_MILLIS);
			String downloadResourceReference = variables
					.getString(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_REFERENCE);

			return Stream.concat(
					Stream.of(DownloadedBytesGenerator.create(downloadedBytes),
							DownloadedDurationMillisGenerator.create(downloadedDurationMillis),
							DownloadResourceReferenceGenerator.create(downloadResourceReference)),
					ErrorMessageGenerator.create(errorList).stream());
		}
		else
		{
			return ErrorMessageGenerator.create(errorList).stream();
		}
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws Exception
	{
		Target target = variables.getTarget();
		super.doExecute(execution, variables);

		Task mainTask = variables.getStartTask();
		mainTask.addOutput(PingStatusGenerator.createPongStatusOutput(target,
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_SENT));
		variables.updateTask(mainTask);
	}

	@Override
	protected void handleIntermediateThrowEventError(DelegateExecution execution, Variables variables,
			Exception exception, String errorMessage)
	{
		String statusCode = exception instanceof WebApplicationException w && w.getResponse() != null
				&& w.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()
						? ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED
						: ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE;
		execution.setVariable(ConstantsPing.getBpmnExecutionVariableStatusCode(), statusCode);

		String specialErrorMessage = createErrorMessage(exception);
		execution.setVariable(ConstantsPing.getBpmnExecutionVariableErrorMessage(), specialErrorMessage);
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
