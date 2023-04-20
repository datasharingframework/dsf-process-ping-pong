package dev.dsf.bpe.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.bpe.documentation.ProcessDocumentation;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.message.SendPing;
import dev.dsf.bpe.message.SendPong;
import dev.dsf.bpe.message.SendStartPing;
import dev.dsf.bpe.service.LogNoResponse;
import dev.dsf.bpe.service.LogPing;
import dev.dsf.bpe.service.LogPong;
import dev.dsf.bpe.service.MailService;
import dev.dsf.bpe.service.SelectPingTargets;
import dev.dsf.bpe.service.SelectPongTarget;
import dev.dsf.bpe.service.SetTargetAndConfigureTimer;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.fhir.authorization.read.ReadAccessHelper;
import dev.dsf.fhir.client.FhirWebserviceClientProvider;
import dev.dsf.fhir.organization.EndpointProvider;
import dev.dsf.fhir.organization.OrganizationProvider;
import dev.dsf.fhir.task.TaskHelper;

@Configuration
public class PingConfig
{
	@Autowired
	private FhirWebserviceClientProvider clientProvider;

	@Autowired
	private TaskHelper taskHelper;

	@Autowired
	private ReadAccessHelper readAccessHelper;

	@Autowired
	private OrganizationProvider organizationProvider;

	@Autowired
	private EndpointProvider endpointProvider;

	@Autowired
	private FhirContext fhirContext;

	@Autowired
	private MailService mailService;

	@ProcessDocumentation(description = "To enable a mail being send if the ping process fails, set to 'true'. This requires the SMPT mail service client to be configured in the DSF", processNames = "dsfdev_ping")
	@Value("${dev.dsf.dsf.bpe.ping.mail.onPingProcessFailed:false}")
	boolean sendPingProcessFailedMail;

	@ProcessDocumentation(description = "To enable a mail being send if the pong process fails, set to 'true'. This requires the SMPT mail service client to be configured in the DSF", processNames = "dsfdev_pong")
	@Value("${dev.dsf.dsf.bpe.ping.mail.onPongProcessFailed:false}")
	boolean sendPongProcessFailedMail;

	@Bean
	public SetTargetAndConfigureTimer setTargetAndConfigureTimer()
	{
		return new SetTargetAndConfigureTimer(clientProvider, taskHelper, readAccessHelper, organizationProvider,
				endpointProvider);
	}

	@Bean
	public SendStartPing sendStartPing()
	{
		return new SendStartPing(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Bean
	public PingStatusGenerator responseGenerator()
	{
		return new PingStatusGenerator();
	}

	@Bean
	public ErrorMailService errorLogger()
	{
		return new ErrorMailService(mailService, clientProvider, organizationProvider.getLocalIdentifierValue(),
				sendPingProcessFailedMail, sendPongProcessFailedMail);
	}

	@Bean
	public SendPing sendPing()
	{
		return new SendPing(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext,
				responseGenerator(), errorLogger());
	}

	@Bean
	public SendPong sendPong()
	{
		return new SendPong(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext,
				responseGenerator(), errorLogger());
	}

	@Bean
	public LogPing logPing()
	{
		return new LogPing(clientProvider, taskHelper, readAccessHelper);
	}

	@Bean
	public LogPong logPong()
	{
		return new LogPong(clientProvider, taskHelper, readAccessHelper, responseGenerator());
	}

	@Bean
	public LogNoResponse logNoResponse()
	{
		return new LogNoResponse(clientProvider, taskHelper, readAccessHelper, responseGenerator(), errorLogger());
	}

	@Bean
	public SelectPingTargets selectPingTargets()
	{
		return new SelectPingTargets(clientProvider, taskHelper, readAccessHelper, organizationProvider);
	}

	@Bean
	public SelectPongTarget selectPongTarget()
	{
		return new SelectPongTarget(clientProvider, taskHelper, readAccessHelper, endpointProvider);
	}
}
