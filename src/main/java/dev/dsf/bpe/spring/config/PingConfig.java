package dev.dsf.bpe.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import dev.dsf.bpe.listener.RemoveCorrelationKeyListener;
import dev.dsf.bpe.listener.SetCorrelationKeyListener;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.message.CleanupPong;
import dev.dsf.bpe.message.SendPing;
import dev.dsf.bpe.message.SendPong;
import dev.dsf.bpe.message.SendStartPing;
import dev.dsf.bpe.service.Cleanup;
import dev.dsf.bpe.service.DownloadResourceAndMeasureSpeed;
import dev.dsf.bpe.service.EstimateCleanupTimerDuration;
import dev.dsf.bpe.service.GenerateResource;
import dev.dsf.bpe.service.LogAndSaveAndStoreError;
import dev.dsf.bpe.service.LogAndSaveError;
import dev.dsf.bpe.service.LogAndSaveNoResponse;
import dev.dsf.bpe.service.LogAndSaveSendError;
import dev.dsf.bpe.service.LogAndStoreSendError;
import dev.dsf.bpe.service.LogNoResponse;
import dev.dsf.bpe.service.LogPing;
import dev.dsf.bpe.service.LogSendError;
import dev.dsf.bpe.service.SaveDownloadSpeeds;
import dev.dsf.bpe.service.SavePong;
import dev.dsf.bpe.service.SelectPingTargets;
import dev.dsf.bpe.service.SelectPongTarget;
import dev.dsf.bpe.service.SetDownloadResourceSize;
import dev.dsf.bpe.service.SetTargetAndConfigureTimer;
import dev.dsf.bpe.service.StoreDownloadSpeeds;
import dev.dsf.bpe.service.StoreResource;
import dev.dsf.bpe.service.StoreResults;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.documentation.ProcessDocumentation;

@Configuration
public class PingConfig
{
	@Autowired
	private ProcessPluginApi api;

	@ProcessDocumentation(description = "To enable a mail being sent if the ping process fails, set to 'true'. This requires the SMPT mail service client to be configured in the DSF", processNames = "dsfdev_ping")
	@Value("${dev.dsf.dsf.bpe.ping.mail.onPingProcessFailed:false}")
	private boolean sendPingProcessFailedMail;

	@ProcessDocumentation(description = "To enable a mail being sent if the pong process fails, set to 'true'. This requires the SMPT mail service client to be configured in the DSF", processNames = "dsfdev_pong")
	@Value("${dev.dsf.dsf.bpe.ping.mail.onPongProcessFailed:false}")
	private boolean sendPongProcessFailedMail;

	@ProcessDocumentation(description = "Sets the download limit on resource downloads, essentially limiting the amount of data downloaded from other ping instances. Setting this to a negative value will disable resource downloads, effectively resulting in running the slim (\"old\") ping process.")
	@Value("${dev.dsf.bpe.ping.maxDownloadSize:10000000}")
	private long maxDownloadSizeBytes;

	@ProcessDocumentation(description = "Sets the upload limit on resource uploads, essentially limiting the amount of data other ping instances are able to download from this instance.")
	@Value("${dev.dsf.bpe.ping.maxUploadSize:10000000}")
	private long maxUploadSizeBytes;

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
	public StoreResults savePingResults()
	{
		return new StoreResults(api, responseGenerator(), errorLogger());
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

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StoreResource storeResource()
	{
		return new StoreResource(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogAndSaveNoResponse logAndSaveNoResponse()
	{
		return new LogAndSaveNoResponse(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public CleanupPong cleanupPong()
	{
		return new CleanupPong(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DownloadResourceAndMeasureSpeed downloadResourceAndMeasureSpeed()
	{
		return new DownloadResourceAndMeasureSpeed(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public GenerateResource generateResource()
	{
		return new GenerateResource(api, (int) maxUploadSizeBytes);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogAndSaveSendError logAndSaveSendError()
	{
		return new LogAndSaveSendError(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogAndStoreSendError logAndStoreSendError()
	{
		return new LogAndStoreSendError(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Cleanup cleanup()
	{
		return new Cleanup(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogAndSaveAndStoreError logAndSaveAndStoreError()
	{
		return new LogAndSaveAndStoreError(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogAndSaveError logAndSaveError()
	{
		return new LogAndSaveError(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SaveDownloadSpeeds saveDownloadSpeeds()
	{
		return new SaveDownloadSpeeds(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StoreDownloadSpeeds storeDownloadSpeeds()
	{
		return new StoreDownloadSpeeds(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public EstimateCleanupTimerDuration estimateCleanupTimerDuration()
	{
		return new EstimateCleanupTimerDuration(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SetDownloadResourceSize setDownloadResourceSize()
	{
		return new SetDownloadResourceSize(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SavePong savePong()
	{
		return new SavePong(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public RemoveCorrelationKeyListener removeCorrelationKeyListener()
	{
		return new RemoveCorrelationKeyListener(api);
	}
}
