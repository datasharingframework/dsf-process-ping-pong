package dev.dsf.bpe.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import dev.dsf.bpe.listener.SetCorrelationKeyListener;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.message.SendPing;
import dev.dsf.bpe.message.SendPong;
import dev.dsf.bpe.message.SendStartPing;
import dev.dsf.bpe.service.LogNoResponse;
import dev.dsf.bpe.service.LogPing;
import dev.dsf.bpe.service.LogPong;
import dev.dsf.bpe.service.LogSendError;
import dev.dsf.bpe.service.SaveResults;
import dev.dsf.bpe.service.SelectPingTargets;
import dev.dsf.bpe.service.SelectPongTarget;
import dev.dsf.bpe.service.SetTargetAndConfigureTimer;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.documentation.ProcessDocumentation;

@Configuration
public class PingConfig
{
	@Autowired
	private ProcessPluginApi api;

	@ProcessDocumentation(description = "To enable a mail being send if the ping process fails, set to 'true'. This requires the SMPT mail service client to be configured in the DSF", processNames = "dsfdev_ping")
	@Value("${dev.dsf.dsf.bpe.ping.mail.onPingProcessFailed:false}")
	private boolean sendPingProcessFailedMail;

	@ProcessDocumentation(description = "To enable a mail being send if the pong process fails, set to 'true'. This requires the SMPT mail service client to be configured in the DSF", processNames = "dsfdev_pong")
	@Value("${dev.dsf.dsf.bpe.ping.mail.onPongProcessFailed:false}")
	private boolean sendPongProcessFailedMail;

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SetTargetAndConfigureTimer setTargetAndConfigureTimer()
	{
		return new SetTargetAndConfigureTimer(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SendStartPing sendStartPing()
	{
		return new SendStartPing(api);
	}

	@Bean
	public PingStatusGenerator responseGenerator()
	{
		return new PingStatusGenerator();
	}

	@Bean
	public ErrorMailService errorLogger()
	{
		return new ErrorMailService(api, sendPingProcessFailedMail, sendPongProcessFailedMail);
	}


	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SelectPingTargets selectPingTargets()
	{
		return new SelectPingTargets(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SendPing sendPing()
	{
		return new SendPing(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SetCorrelationKeyListener setCorrelationKeyListener()
	{
		return new SetCorrelationKeyListener(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogPong logPong()
	{
		return new LogPong(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogNoResponse logNoResponse()
	{
		return new LogNoResponse(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogSendError logSendError()
	{
		return new LogSendError(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SaveResults savePingResults()
	{
		return new SaveResults(api, responseGenerator(), errorLogger());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogPing logPing()
	{
		return new LogPing(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SelectPongTarget selectPongTarget()
	{
		return new SelectPongTarget(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SendPong sendPong()
	{
		return new SendPong(api, responseGenerator(), errorLogger());
	}
}
