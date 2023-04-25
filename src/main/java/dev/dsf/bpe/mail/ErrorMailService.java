package dev.dsf.bpe.mail;

import java.util.Objects;

import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Target;

public class ErrorMailService implements InitializingBean
{
	private static final Logger pingProcessErrorLogger = LoggerFactory.getLogger("ping-process-error-logger");
	private static final Logger pongProcessErrorLogger = LoggerFactory.getLogger("pong-process-error-logger");

	private static final String SUBJECT_PING_PROCESS_FAILED = "Ping Process Failed";
	private static final String SUBJECT_PONG_PROCESS_FAILED = "Pong Process Failed";

	private final ProcessPluginApi api;

	private final boolean sendPingProcessFailedMail;
	private final boolean sendPongProcessFailedMail;

	public ErrorMailService(ProcessPluginApi api, boolean sendPingProcessFailedMail, boolean sendPongProcessFailedMail)
	{
		this.api = api;

		this.sendPingProcessFailedMail = sendPingProcessFailedMail;
		this.sendPongProcessFailedMail = sendPongProcessFailedMail;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(api, "api");
	}

	private String createMessage(Target target, String message, String messageDetails, IdType taskId)
	{
		StringBuilder b = new StringBuilder();

		b.append(api.getOrganizationProvider().getLocalOrganizationIdentifierValue().orElse("?"));
		b.append('/');
		b.append(api.getEndpointProvider().getLocalEndpointIdentifierValue().orElse("?"));

		b.append(" -> ");

		b.append(target.getOrganizationIdentifierValue());
		b.append('/');
		b.append(target.getEndpointIdentifierValue());

		b.append(": ");
		b.append(message);

		if (messageDetails != null)
		{
			b.append("\n\t");
			b.append(messageDetails);
		}

		b.append("\n\nProcess started by: ");
		b.append(taskId.toVersionless().withServerBase(api.getEndpointProvider().getLocalEndpointAddress(), "Task")
				.getValue());

		return b.toString();
	}

	public void pongMessageNotReceived(IdType taskId, Target target)
	{
		pingProcessErrorLogger.debug("No pong from organization '{}', Endpoint '{}' received",
				target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue());

		if (sendPingProcessFailedMail)
		{
			api.getMailService().send(SUBJECT_PING_PROCESS_FAILED,
					createMessage(target, "No pong message received", null, taskId));
		}
	}

	public void endpointNotReachableForPing(IdType taskId, Target target, String errorMessage)
	{
		pingProcessErrorLogger.debug("Endpoint '{}' at organization '{}' not reachable with ping: {}",
				target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue(), errorMessage);

		if (sendPingProcessFailedMail)
		{
			api.getMailService().send(SUBJECT_PING_PROCESS_FAILED,
					createMessage(target, "Not reachable with ping", errorMessage, taskId));
		}
	}

	public void endpointReachablePingForbidden(IdType taskId, Target target, String errorMessage)
	{
		pingProcessErrorLogger.debug("Endpoint '{}' at organization '{}' reachable, ping forbidden: {}",
				target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue(), errorMessage);

		if (sendPongProcessFailedMail)
		{
			api.getMailService().send(SUBJECT_PING_PROCESS_FAILED,
					createMessage(target, "Ping forbidden", errorMessage, taskId));
		}
	}

	public void endpointNotReachableForPong(IdType taskId, Target target, String errorMessage)
	{
		pongProcessErrorLogger.debug("Endpoint '{}' at organization '{}' not reachable with pong: {}",
				target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue(), errorMessage);

		if (sendPongProcessFailedMail)
		{
			api.getMailService().send(SUBJECT_PONG_PROCESS_FAILED,
					createMessage(target, "Not reachable with pong", errorMessage, taskId));
		}
	}

	public void endpointReachablePongForbidden(IdType taskId, Target target, String errorMessage)
	{
		pongProcessErrorLogger.debug("Endpoint '{}' at organization '{}' reachable, pong forbidden: {}",
				target.getOrganizationIdentifierValue(), target.getEndpointIdentifierValue(), errorMessage);

		if (sendPongProcessFailedMail)
		{
			api.getMailService().send(SUBJECT_PONG_PROCESS_FAILED,
					createMessage(target, "Pong forbidden", errorMessage, taskId));
		}
	}
}
