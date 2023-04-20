package dev.dsf.bpe.message;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.bpe.ConstantsBase;
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

public class SendPing extends AbstractTaskMessageSend
{
	private final PingStatusGenerator statusGenerator;
	private final ErrorMailService errorLogger;

	public SendPing(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		return Stream.of(getTaskHelper().createInput(ConstantsPing.CODESYSTEM_DSF_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_ENDPOINT_IDENTIFIER,
				new Reference().setIdentifier(getLocalEndpointIdentifier()).setType(ResourceType.Endpoint.name())));
	}

	@Override
	protected void handleSendTaskError(DelegateExecution execution, Exception exception, String errorMessage)
	{
		Target target = getTarget(execution);
		Task task = getLeadingTaskFromExecutionVariables(execution);

		if (task != null)
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

			task.addOutput(statusGenerator.createPingStatusOutput(target, statusCode, specialErrorMessage));
			updateLeadingTaskInExecutionVariables(execution, task);

			if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE.equals(statusCode))
				errorLogger.endpointNotReachableForPing(task.getIdElement(), target, specialErrorMessage);
			else if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED.equals(statusCode))
				errorLogger.endpointReachablePingForbidden(task.getIdElement(), target, specialErrorMessage);
		}

		super.handleSendTaskError(execution, exception, errorMessage);
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
		Bundle bundle = getFhirWebserviceClientProvider().getLocalWebserviceClient().search(Endpoint.class,
				Map.of("address", Collections.singletonList(getFhirWebserviceClientProvider().getLocalBaseUrl())));
		return bundle.getEntry().stream().filter(BundleEntryComponent::hasResource)
				.filter(e -> e.getResource() instanceof Endpoint).map(e -> (Endpoint) e.getResource()).findFirst()
				.filter(e -> e.hasIdentifier())
				.flatMap(e -> e.getIdentifier().stream()
						.filter(i -> ConstantsBase.NAMINGSYSTEM_DSF_ENDPOINT_IDENTIFIER.equals(i.getSystem()))
						.findFirst())
				.orElseThrow(() -> new IllegalStateException("No Identifier for Endpoint or Endpoint with address "
						+ getFhirWebserviceClientProvider().getLocalBaseUrl() + " found"));
	}
}
