package dev.dsf.bpe.message;

import java.util.Objects;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class SendPing extends AbstractTaskMessageSend
{
	private final PingStatusGenerator statusGenerator;
	private final ErrorMailService errorMailService;

	public SendPing(ProcessPluginApi api, PingStatusGenerator statusGenerator, ErrorMailService errorMailService)
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
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables)
	{
		return Stream.of(api.getTaskHelper().createInput(ConstantsPing.CODESYSTEM_DSF_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_ENDPOINT_IDENTIFIER,
				new Reference().setIdentifier(getLocalEndpointIdentifier()).setType(ResourceType.Endpoint.name())));
	}

	@Override
	protected void handleSendTaskError(DelegateExecution execution, Variables variables, Exception exception,
			String errorMessage)
	{
		Target target = variables.getTarget();
		Task mainTask = variables.getStartTask();

		if (mainTask != null)
		{
			String statusCode = ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE;
			if (exception instanceof WebApplicationException webApplicationException)
			{
				if (webApplicationException.getResponse() != null && webApplicationException.getResponse()
						.getStatus() == Response.Status.FORBIDDEN.getStatusCode())
				{
					statusCode = ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED;
				}
			}

			String specialErrorMessage = createErrorMessage(exception);

			mainTask.addOutput(statusGenerator.createPingStatusOutput(target, statusCode, specialErrorMessage));
			variables.updateTask(mainTask);

			if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE.equals(statusCode))
				errorMailService.endpointNotReachableForPing(mainTask.getIdElement(), target, specialErrorMessage);
			else if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED.equals(statusCode))
				errorMailService.endpointReachablePingForbidden(mainTask.getIdElement(), target, specialErrorMessage);
		}

		super.handleSendTaskError(execution, variables, exception, errorMessage);
	}

	@Override
	protected void addErrorMessage(Task task, String errorMessage)
	{
		// error message part of
	}

	private String createErrorMessage(Exception exception)
	{
		return exception.getClass().getSimpleName()
				+ ((exception.getMessage() != null && !exception.getMessage().isBlank())
						? (": " + exception.getMessage())
						: "");
	}

	private Identifier getLocalEndpointIdentifier()
	{
		return api.getEndpointProvider().getLocalEndpointIdentifier()
				.orElseThrow(() -> new IllegalStateException("Local endpoint identifier unknown"));
	}
}
