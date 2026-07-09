package dev.dsf.bpe.mail;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.variables.Target;

public class AggregateErrorMailService implements InitializingBean
{
	private static final Logger errorMailServiceLogger = LoggerFactory.getLogger("error-mail-service-logger");
	private static final String MAIL_MESSAGE_INTRO = "Error(s) while executing ping-pong process:";

	public static final String PING_PROCESS_HAS_ERRORS = "Ping process has errors";
	public static final String PONG_PROCESS_HAS_ERRORS = "Pong process has errors";

	private final ProcessPluginApi api;
	private final boolean sendProcessFailedMail;
	private final String subject;

	public AggregateErrorMailService(ProcessPluginApi api, boolean sendProcessFailedMail, String subject)
	{
		this.api = api;
		this.sendProcessFailedMail = sendProcessFailedMail;
		this.subject = subject;
	}

	@Override
	public void afterPropertiesSet()
	{
		Objects.requireNonNull(api, "api");
	}

	public void send(IdType taskId, List<ProcessError> localProcessErrors,
			Map<Target, List<ProcessError>> errorsPerTarget)
	{
		if (sendProcessFailedMail && (!localProcessErrors.isEmpty() || hasErrors(errorsPerTarget)))
		{
			api.getMailService().send(subject, buildMailMessage(taskId, localProcessErrors, errorsPerTarget));
			errorMailServiceLogger.info("Sent e-mail with process errors");
		}
	}

	protected String buildMailMessage(IdType taskId, List<ProcessError> localProcessErrors,
			Map<Target, List<ProcessError>> errorsPerTarget)
	{
		StringBuilder mailMessage = new StringBuilder();

		mailMessage.append(MAIL_MESSAGE_INTRO);
		mailMessage.append("\n\n");

		if (localProcessErrors != null)
			localProcessErrors.stream().map(this::createMessage).forEach(message ->
			{
				mailMessage.append(message);
				mailMessage.append("\n\n");
			});

		if (errorsPerTarget != null)
			errorsPerTarget.entrySet().stream()
					.map(entry -> entry.getValue().stream().map(error -> createMessage(entry.getKey(), error)))
					.forEach(messageStream -> messageStream.forEach(message ->
					{
						mailMessage.append(message);
						mailMessage.append("\n\n");
					}));

		mailMessage.append("Process started by: ");
		mailMessage.append(taskId.toVersionless()
				.withServerBase(api.getEndpointProvider().getLocalEndpointAddress(), "Task").getValue());

		return mailMessage.toString();
	}

	protected String createMessage(Target target, ProcessError error)
	{
		StringBuilder b = new StringBuilder();

		if (error != null && error.process() != null)
		{
			b.append(api.getOrganizationProvider().getLocalOrganizationIdentifierValue().orElse("?"));
			b.append('/');
			b.append(api.getEndpointProvider().getLocalEndpointIdentifierValue().orElse("?"));

			b.append(" -> ");

			b.append(target.getOrganizationIdentifierValue());
			b.append('/');
			b.append(target.getEndpointIdentifierValue());

			b.append(":");
			b.append("\n\t");
			b.append("Description: ").append(error.concept().getDisplay());
		}
		else
		{
			b.append("Other:");
			b.append("\n\t");
			b.append("Description: ").append(error.concept().getDisplay());
		}

		return b.toString();
	}

	protected String createMessage(ProcessError error)
	{
		StringBuilder b = new StringBuilder();

		if (error != null && error.concept() != null)
		{
			b.append("Error during local process execution:");
			b.append("\n\t");
			b.append("Description: ").append(error.concept().getDisplay());
		}

		return b.toString();
	}

	private boolean hasErrors(Map<Target, List<ProcessError>> errorsPerTarget)
	{
		return errorsPerTarget.values().stream().anyMatch(errors -> !errors.isEmpty());
	}
}
