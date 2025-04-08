package dev.dsf.bpe.mail;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.IdType;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Target;

public class AggregateErrorMailService extends ErrorMailService
{
	private static final String MAIL_MESSAGE_INTRO = "Error(s) while executing ping-pong process:";
	private static final String PING_PROCESS_HAS_ERRORS = "Ping process has errors";

	private List<String> errorMessages;

	public AggregateErrorMailService(ProcessPluginApi api, boolean sendPingProcessFailedMail,
			boolean sendPongProcessFailedMail)
	{
		super(api, sendPingProcessFailedMail, sendPongProcessFailedMail);
		errorMessages = new ArrayList<>();
	}

	public void addMessagePing(Target target, String message)
	{
		pingProcessErrorLogger.info("Ping process error: {}", message);

		if (sendPingProcessFailedMail)
		{
			errorMessages.add(createMessage(target, message, null));
		}
	}

	public void addMessagePong(Target target, String message)
	{
		pongProcessErrorLogger.info("Pong process error: {}", message);

		if (sendPongProcessFailedMail)
		{
			errorMessages.add(createMessage(target, message, null));
		}
	}

	public void send(IdType taskId)
	{
		if (!errorMessages.isEmpty())
		{
			api.getMailService().send(PING_PROCESS_HAS_ERRORS, buildMailMessage(taskId));
			errorMessages = new ArrayList<>();
		}
	}

	protected String buildMailMessage(IdType taskId)
	{
		StringBuilder mailMessage = new StringBuilder();

		mailMessage.append(MAIL_MESSAGE_INTRO);
		mailMessage.append("\n\n");

		errorMessages.forEach(errorMessage ->
		{
			mailMessage.append(errorMessage);
			mailMessage.append("\n\n");
		});

		mailMessage.append("\nProcess started by: ");
		mailMessage.append(taskId.toVersionless()
				.withServerBase(api.getEndpointProvider().getLocalEndpointAddress(), "Task").getValue());

		return mailMessage.toString();
	}

	protected String createMessage(Target target, String message, String messageDetails)
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

		return b.toString();
	}
}
