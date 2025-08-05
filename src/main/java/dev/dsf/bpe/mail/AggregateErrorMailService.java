package dev.dsf.bpe.mail;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Target;

public class AggregateErrorMailService implements InitializingBean
{
	private static final Logger errorMailServiceLogger = LoggerFactory.getLogger("error-mail-service-logger");
	private static final String MAIL_MESSAGE_INTRO = "Error(s) while executing ping-pong process:";
	private static final String PING_PROCESS_HAS_ERRORS = "Ping process has errors";

	private final ProcessPluginApi api;
	private final boolean sendProcessFailedMail;

	public AggregateErrorMailService(ProcessPluginApi api, boolean sendProcessFailedMail)
	{
		this.api = api;
		this.sendProcessFailedMail = sendProcessFailedMail;
	}

	@Override
	public void afterPropertiesSet()
	{
		Objects.requireNonNull(api, "api");
	}

	public void send(IdType taskId, Map<Target, List<ProcessError>> errorsPerTarget)
	{
		if (sendProcessFailedMail)
		{
			api.getMailService().send(PING_PROCESS_HAS_ERRORS, buildMailMessage(taskId, errorsPerTarget));
			errorMailServiceLogger.info("Sent e-mail with process errors");
		}
	}

	protected String buildMailMessage(IdType taskId, Map<Target, List<ProcessError>> errorsPerTarget)
	{
		StringBuilder mailMessage = new StringBuilder();

		mailMessage.append(MAIL_MESSAGE_INTRO);
		mailMessage.append("\n\n");

		errorsPerTarget.entrySet().stream()
				.map(entry -> entry.getValue().stream().map(error -> createMessage(entry.getKey(), error)))
				.forEach(messageStream -> messageStream.forEach(message ->
				{
					mailMessage.append(message);
					mailMessage.append("\n\n");
				}));

		mailMessage.append("Process started by: ");
		mailMessage.append(
				taskId.toVersionless().withServerBase(api.getEndpointProvider().getLocalEndpointAddress(), "Task").getValue());

		return mailMessage.toString();
	}

	protected String createMessage(Target target, ProcessError error)
	{
		StringBuilder b = new StringBuilder();

		if (error != null && error.process() != null)
		{
			if (CodeSystem.DsfPingProcesses.Code.PING.equals(error.process()))
			{
				b.append(api.getOrganizationProvider().getLocalOrganizationIdentifierValue().orElse("?"));
				b.append('/');
				b.append(api.getEndpointProvider().getLocalEndpointIdentifierValue().orElse("?"));

				b.append(" -> ");

				b.append(target.getOrganizationIdentifierValue());
				b.append('/');
				b.append(target.getEndpointIdentifierValue());

				b.append(":");
			}
			else
			{
				b.append(target.getOrganizationIdentifierValue());
				b.append('/');
				b.append(target.getEndpointIdentifierValue());

				b.append(" -> ");

				b.append(api.getOrganizationProvider().getLocalOrganizationIdentifierValue().orElse("?"));
				b.append('/');
				b.append(api.getEndpointProvider().getLocalEndpointIdentifierValue().orElse("?"));

				b.append(": ");
			}
			b.append("\n\t");
			b.append("Process: ").append(error.process());
			b.append("\n\t");
			b.append("Process step: ").append(error.processStep());
			b.append("\n\t");
			b.append("Action: ").append(error.action());
			b.append("\n\t");
			b.append("Message: ").append(error.message());
		}
		else
		{
			b.append("Unable to display error because error is null or process is neither of 'ping' or 'pong'");
		}

		return b.toString();
	}
}
