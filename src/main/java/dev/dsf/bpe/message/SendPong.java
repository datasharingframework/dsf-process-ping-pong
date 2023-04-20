package dev.dsf.bpe.message;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.fhir.authorization.read.ReadAccessHelper;
import dev.dsf.fhir.client.FhirWebserviceClientProvider;
import dev.dsf.fhir.organization.OrganizationProvider;
import dev.dsf.fhir.task.AbstractTaskMessageSend;
import dev.dsf.fhir.task.TaskHelper;
import dev.dsf.fhir.variables.Target;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class SendPong extends AbstractTaskMessageSend
{
	private final PingStatusGenerator statusGenerator;
	private final ErrorMailService errorLogger;

	public SendPong(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext,
			PingStatusGenerator statusGenerator, ErrorMailService errorLogger)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);

		this.statusGenerator = statusGenerator;
		this.errorLogger = errorLogger;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(statusGenerator, "statusGenerator");
		Objects.requireNonNull(errorLogger, "errorLogger");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		super.doExecute(execution);

		Target target = getTarget(execution);
		Task task = getLeadingTaskFromExecutionVariables(execution);
		task.addOutput(statusGenerator.createPongStatusOutput(target,
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_SEND));
		updateLeadingTaskInExecutionVariables(execution, task);
	}

	@Override
	protected void handleEndEventError(DelegateExecution execution, Exception exception, String errorMessage)
	{
		Target target = getTarget(execution);
		Task task = getLeadingTaskFromExecutionVariables(execution);

		if (task != null)
		{
			String statusCode = ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE;
			if (exception instanceof WebApplicationException)
			{
				WebApplicationException webApplicationException = (WebApplicationException) exception;
				if (webApplicationException.getResponse() != null && webApplicationException.getResponse()
						.getStatus() == Response.Status.FORBIDDEN.getStatusCode())
				{
					statusCode = ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED;
				}
			}

			String specialErrorMessage = createErrorMessage(exception);

			task.addOutput(statusGenerator.createPongStatusOutput(target, statusCode, specialErrorMessage));
			updateLeadingTaskInExecutionVariables(execution, task);

			if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE.equals(statusCode))
				errorLogger.endpointNotReachableForPong(task.getIdElement(), target, specialErrorMessage);
			else if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED.equals(statusCode))
				errorLogger.endpointReachablePongForbidden(task.getIdElement(), target, specialErrorMessage);
		}

		super.handleEndEventError(execution, exception, errorMessage);
	}

	private String createErrorMessage(Exception exception)
	{
		return exception.getClass().getSimpleName()
				+ ((exception.getMessage() != null && !exception.getMessage().isBlank())
						? (": " + exception.getMessage())
						: "");
	}
}
