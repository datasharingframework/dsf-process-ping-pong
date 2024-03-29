package dev.dsf.bpe.message;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.StatusType;

public class SendPong extends AbstractTaskMessageSend
{
	private final PingStatusGenerator statusGenerator;
	private final ErrorMailService errorMailService;

	public SendPong(ProcessPluginApi api, PingStatusGenerator statusGenerator, ErrorMailService errorMailService)
	{
		super(api);

		this.statusGenerator = statusGenerator;
		this.errorMailService = errorMailService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(statusGenerator, "statusGenerator");
		Objects.requireNonNull(errorMailService, "errorMailService");
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws Exception
	{
		super.doExecute(execution, variables);

		Target target = variables.getTarget();
		Task mainTask = variables.getStartTask();
		mainTask.addOutput(statusGenerator.createPongStatusOutput(target,
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_SEND));
		variables.updateTask(mainTask);
	}

	@Override
	protected void handleEndEventError(DelegateExecution execution, Variables variables, Exception exception,
			String errorMessage)
	{
		Target target = variables.getTarget();
		Task mainTask = variables.getStartTask();

		if (mainTask != null)
		{
			String statusCode = exception instanceof WebApplicationException w && w.getResponse() != null
					&& w.getResponse().getStatus() == Response.Status.FORBIDDEN.getStatusCode()
							? ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED
							: ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE;

			String specialErrorMessage = createErrorMessage(exception);

			mainTask.addOutput(statusGenerator.createPongStatusOutput(target, statusCode, specialErrorMessage));
			variables.updateTask(mainTask);

			if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE.equals(statusCode))
				errorMailService.endpointNotReachableForPong(mainTask.getIdElement(), target, specialErrorMessage);
			else if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED.equals(statusCode))
				errorMailService.endpointReachablePongForbidden(mainTask.getIdElement(), target, specialErrorMessage);
		}

		super.handleEndEventError(execution, variables, exception, errorMessage);
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
