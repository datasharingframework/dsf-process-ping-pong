package dev.dsf.bpe.spring.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.listener.PingPongProcessPluginDeploymentStateListener;
import dev.dsf.bpe.listener.SetCorrelationKeyListener;
import dev.dsf.bpe.mail.AggregateErrorMailService;
import dev.dsf.bpe.message.CleanupPongMessage;
import dev.dsf.bpe.message.SendPingMessage;
import dev.dsf.bpe.message.SendPongMessage;
import dev.dsf.bpe.message.SendStartPing;
import dev.dsf.bpe.service.Cleanup;
import dev.dsf.bpe.service.GenerateAndStoreResource;
import dev.dsf.bpe.service.SetDownloadResourceSize;
import dev.dsf.bpe.service.autostart.SetTargetAndConfigureTimer;
import dev.dsf.bpe.service.ping.CheckPingTaskStatus;
import dev.dsf.bpe.service.ping.DownloadResourceAndMeasureSpeedInSubProcess;
import dev.dsf.bpe.service.ping.LogAndSaveError;
import dev.dsf.bpe.service.ping.LogAndSaveSendError;
import dev.dsf.bpe.service.ping.LogAndSaveUploadErrorPing;
import dev.dsf.bpe.service.ping.SavePong;
import dev.dsf.bpe.service.ping.SelectPingTargets;
import dev.dsf.bpe.service.ping.SetPongTimeoutDuration;
import dev.dsf.bpe.service.ping.StoreResults;
import dev.dsf.bpe.service.pong.DownloadResourceAndMeasureSpeed;
import dev.dsf.bpe.service.pong.EstimateCleanupTimerDuration;
import dev.dsf.bpe.service.pong.LogAndSaveAndStoreError;
import dev.dsf.bpe.service.pong.LogAndSaveUploadErrorPong;
import dev.dsf.bpe.service.pong.LogPing;
import dev.dsf.bpe.service.pong.SaveTimeoutError;
import dev.dsf.bpe.service.pong.SelectPongTarget;
import dev.dsf.bpe.service.pong.SetEndpointIdentifier;
import dev.dsf.bpe.service.pong.StoreDownloadSpeed;
import dev.dsf.bpe.service.pong.StoreErrors;
import dev.dsf.bpe.service.pong.StoreUploadSpeed;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.documentation.ProcessDocumentation;
import dev.dsf.bpe.v2.spring.ActivityPrototypeBeanCreator;

@Configuration
public class PingConfig implements InitializingBean
{
	@Autowired
	private ProcessPluginApi api;

	@ProcessDocumentation(description = "To enable a mail being sent if the ping process fails, set to 'true'. This requires the SMPT mail service client to be configured in the DSF", processNames = "dsfdev_ping")
	@Value("${dev.dsf.bpe.ping.mail.onPingProcessFailed:false}")
	private boolean sendPingProcessFailedMail;

	@ProcessDocumentation(description = "To enable a mail being sent if the pong process fails, set to 'true'. This requires the SMPT mail service client to be configured in the DSF", processNames = "dsfdev_pong")
	@Value("${dev.dsf.bpe.ping.mail.onPongProcessFailed:false}")
	private boolean sendPongProcessFailedMail;

	@ProcessDocumentation(description = "Sets the download limit on resource downloads, essentially limiting the amount of data downloaded from other ping instances. Setting this to a negative value will disable resource downloads, effectively resulting in running the slim (v1.x) ping process.", processNames = {
			"dsfdev_ping", "dsfdev_pong" })
	@Value("${dev.dsf.bpe.ping.max.download.size.bytes:400000000}")
	private long maxDownloadSizeBytes;

	@ProcessDocumentation(description = "Sets the upload limit on resource uploads, essentially limiting the amount of data other ping instances are able to download from this instance.", processNames = {
			"dsfdev_ping", "dsfdev_pong" })
	@Value("${dev.dsf.bpe.ping.max.upload.size.bytes:400000000}")
	private long maxUploadSizeBytes;

	@ProcessDocumentation(description = "Unit to display upload and download speeds in. Eligible values are: \"bps\", \"kbps\", \"Mbps\", \"Gbps\", \"Bps\", \"kBps\", \"MBps\", \"GBps\". If unset, the process will try to fit the network speed to appropriate units.", processNames = {
			"dsfdev_ping", "dsfdev_pong" })
	@Value("${dev.dsf.bpe.ping.network.speed.unit:#{null}}")
	private CodeSystem.DsfPingUnits.Code networkSpeedUnit;

	public CodeSystem.DsfPingUnits.Code getNetworkSpeedUnit()
	{
		return networkSpeedUnit;
	}

	public void setNetworkSpeedUnit(CodeSystem.DsfPingUnits.Code networkSpeedUnit)
	{
		this.networkSpeedUnit = networkSpeedUnit;
	}

	public long getMaxDownloadSizeBytes()
	{
		return maxDownloadSizeBytes;
	}

	public void setMaxDownloadSizeBytes(long maxDownloadSizeBytes)
	{
		this.maxDownloadSizeBytes = maxDownloadSizeBytes;
	}

	public long getMaxUploadSizeBytes()
	{
		return maxUploadSizeBytes;
	}

	public void setMaxUploadSizeBytes(long maxUploadSizeBytes)
	{
		this.maxUploadSizeBytes = maxUploadSizeBytes;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		fixMaxResourceSizes();
	}

	@Bean
	public static ActivityPrototypeBeanCreator activityPrototypeBeanCreator()
	{
		return new ActivityPrototypeBeanCreator(SetTargetAndConfigureTimer.class, SendStartPing.class,
				SetPongTimeoutDuration.class, SelectPingTargets.class, SendPingMessage.class,
				SetCorrelationKeyListener.class, LogPing.class, SelectPongTarget.class, CheckPingTaskStatus.class,
				CleanupPongMessage.class, DownloadResourceAndMeasureSpeed.class,
				DownloadResourceAndMeasureSpeedInSubProcess.class, Cleanup.class, LogAndSaveAndStoreError.class,
				LogAndSaveError.class, EstimateCleanupTimerDuration.class, SavePong.class, SetEndpointIdentifier.class,
				LogAndSaveSendError.class, SaveTimeoutError.class, LogAndSaveUploadErrorPing.class,
				LogAndSaveUploadErrorPong.class);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public PingPongProcessPluginDeploymentStateListener pingPongProcessPluginDeploymentStateListener()
	{
		return new PingPongProcessPluginDeploymentStateListener(api);
	}

	@Bean
	public PingStatusGenerator pingStatusGenerator()
	{
		return new PingStatusGenerator(api.getProcessPluginDefinition().getResourceVersion());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SendPongMessage sendPongMessage(PingStatusGenerator pingStatusGenerator)
	{
		return new SendPongMessage(pingStatusGenerator);
	}

	@Bean
	public AggregateErrorMailService aggregateErrorMailServicePing()
	{
		return new AggregateErrorMailService(api, sendPingProcessFailedMail,
				AggregateErrorMailService.PING_PROCESS_HAS_ERRORS);
	}

	@Bean
	public AggregateErrorMailService aggregateErrorMailServicePong()
	{
		return new AggregateErrorMailService(api, sendPongProcessFailedMail,
				AggregateErrorMailService.PONG_PROCESS_HAS_ERRORS);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StoreResults savePingResults(PingStatusGenerator pingStatusGenerator)
	{
		return new StoreResults(aggregateErrorMailServicePing(), networkSpeedUnit, pingStatusGenerator);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StoreUploadSpeed storeUploadSpeed(PingStatusGenerator pingStatusGenerator)
	{
		return new StoreUploadSpeed(networkSpeedUnit, pingStatusGenerator);
	}


	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SetDownloadResourceSize setDownloadResourceSize()
	{
		return new SetDownloadResourceSize(maxDownloadSizeBytes);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StoreDownloadSpeed storeDownloadSpeed(PingStatusGenerator pingStatusGenerator)
	{
		return new StoreDownloadSpeed(networkSpeedUnit, pingStatusGenerator);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public GenerateAndStoreResource generateAndStoreResource()
	{
		return new GenerateAndStoreResource(maxUploadSizeBytes);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StoreErrors storeErrors(PingStatusGenerator pingStatusGenerator)
	{
		return new StoreErrors(aggregateErrorMailServicePong(), pingStatusGenerator);
	}

	private void fixMaxResourceSizes()
	{
		if (getMaxDownloadSizeBytes() < 0)
			setMaxDownloadSizeBytes(0);
		if (getMaxUploadSizeBytes() < 0)
			setMaxUploadSizeBytes(0);
	}
}
