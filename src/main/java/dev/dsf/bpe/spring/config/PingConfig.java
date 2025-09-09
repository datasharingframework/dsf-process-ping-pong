package dev.dsf.bpe.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.dsf.bpe.CodeSystem;
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
import dev.dsf.bpe.service.ping.LogAndSaveUploadErrorPing;
import dev.dsf.bpe.service.ping.SavePong;
import dev.dsf.bpe.service.ping.SelectPingTargets;
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
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.documentation.ProcessDocumentation;
import dev.dsf.bpe.variables.duration.DurationValueSerializer;
import dev.dsf.bpe.variables.process_error.ProcessErrorValueSerializer;
import dev.dsf.bpe.variables.process_errors.ProcessErrorsValueSerializer;

@Configuration
public class PingConfig
{
	@Autowired
	private ProcessPluginApi api;

	@ProcessDocumentation(description = "To enable a mail being sent if the ping process fails, set to 'true'. This requires the SMPT mail service client to be configured in the DSF", processNames = "dsfdev_ping")
	@Value("${dev.dsf.bpe.ping.mail.onPingProcessFailed:false}")
	private boolean sendPingProcessFailedMail;

	@ProcessDocumentation(description = "To enable a mail being sent if the pong process fails, set to 'true'. This requires the SMPT mail service client to be configured in the DSF", processNames = "dsfdev_pong")
	@Value("${dev.dsf.bpe.ping.mail.onPongProcessFailed:false}")
	private boolean sendPongProcessFailedMail;

	@ProcessDocumentation(description = "Sets the download limit on resource downloads, essentially limiting the amount of data downloaded from other ping instances. Setting this to a negative value will disable resource downloads, effectively resulting in running the slim (\"old\") ping process.", processNames = "dsfdev_ping, dsfdev_pong")
	@Value("${dev.dsf.bpe.ping.maxDownloadSizeBytes:10000000}")
	private long maxDownloadSizeBytes;

	@ProcessDocumentation(description = "Sets the upload limit on resource uploads, essentially limiting the amount of data other ping instances are able to download from this instance.", processNames = {
			"dsfdev_ping", "dsfdev_pong" })
	@Value("${dev.dsf.bpe.ping.maxUploadSizeBytes:10000000}")
	private long maxUploadSizeBytes;

	@ProcessDocumentation(description = "Unit to display upload and download speeds in. Eligible values are: \"bps\", \"kbps\", \"Mbps\", \"Gbps\", \"Bps\", \"kBps\", \"MBps\", \"GBps\". Default is \"mbps\".", processNames = {
			"dsfdev_ping", "dsfdev_pong" })
	@Value("${dev.dsf.bpe.ping.networkSpeedUnit:Mbps}")
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
	public AggregateErrorMailService aggregateErrorMailServicePing()
	{
		return new AggregateErrorMailService(api, sendPingProcessFailedMail);
	}

	@Bean
	public AggregateErrorMailService aggregateErrorMailServicePong()
	{
		return new AggregateErrorMailService(api, sendPongProcessFailedMail);
	}


	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SelectPingTargets selectPingTargets()
	{
		return new SelectPingTargets(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SendPingMessage sendPing()
	{
		return new SendPingMessage(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SetCorrelationKeyListener setCorrelationKeyListener()
	{
		return new SetCorrelationKeyListener(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StoreResults savePingResults()
	{
		return new StoreResults(api, aggregateErrorMailServicePing(), networkSpeedUnit);
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
	public SendPongMessage sendPong()
	{
		return new SendPongMessage(api, aggregateErrorMailServicePong());
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public CheckPingTaskStatus logAndSaveNoResponse()
	{
		return new CheckPingTaskStatus(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public CleanupPongMessage cleanupPongMessage()
	{
		return new CleanupPongMessage(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DownloadResourceAndMeasureSpeed downloadResourceAndMeasureSpeed()
	{
		return new DownloadResourceAndMeasureSpeed(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DownloadResourceAndMeasureSpeedInSubProcess downloadResourceAndMeasureSpeedInSubProcess()
	{
		return new DownloadResourceAndMeasureSpeedInSubProcess(api);
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
	public StoreUploadSpeed storeDownloadSpeeds()
	{
		return new StoreUploadSpeed(api, networkSpeedUnit);
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
		return new SetDownloadResourceSize(api, maxDownloadSizeBytes);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SavePong savePong()
	{
		return new SavePong(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SetEndpointIdentifier setEndpointIdentifier()
	{
		return new SetEndpointIdentifier(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StoreDownloadSpeed storeDownloadSpeed()
	{
		return new StoreDownloadSpeed(api, networkSpeedUnit);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public dev.dsf.bpe.service.ping.LogAndSaveSendError logAndSaveSendError()
	{
		return new dev.dsf.bpe.service.ping.LogAndSaveSendError(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public GenerateAndStoreResource generateAndStoreResource()
	{
		return new GenerateAndStoreResource(api, maxUploadSizeBytes);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public SaveTimeoutError saveTimeoutError()
	{
		return new SaveTimeoutError(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public StoreErrors storeErrors()
	{
		return new StoreErrors(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogAndSaveUploadErrorPing logAndSaveUploadErrorPing()
	{
		return new LogAndSaveUploadErrorPing(api);
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public LogAndSaveUploadErrorPong logAndSaveUploadErrorPong()
	{
		return new LogAndSaveUploadErrorPong(api);
	}

	@Bean
	public dev.dsf.bpe.variables.codesystem.dsfpingstatus.CodeValueSerializer pingStatusCodeSerializer()
	{
		return new dev.dsf.bpe.variables.codesystem.dsfpingstatus.CodeValueSerializer();
	}

	@Bean
	public DurationValueSerializer durationValueSerializer(
			@Qualifier(OBJECT_MAPPER_WITH_TIME_MODULE) ObjectMapper objectMapper)
	{
		return new DurationValueSerializer(objectMapper);
	}

	@Bean
	public ProcessErrorValueSerializer processErrorValueSerializer()
	{
		return new ProcessErrorValueSerializer();
	}

	@Bean
	public ProcessErrorsValueSerializer processErrorsValueSerializer()
	{
		return new ProcessErrorsValueSerializer();
	}

	@Bean(name = OBJECT_MAPPER_WITH_TIME_MODULE)
	public ObjectMapper objectMapperWithJavaTimeModule()
	{
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		return objectMapper;
	}

	private static final String OBJECT_MAPPER_WITH_TIME_MODULE = "objectMapperWithJavaTimeModule";
}
