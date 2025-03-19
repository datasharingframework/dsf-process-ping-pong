package dev.dsf.bpe.listener;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.spring.config.PingConfig;
import dev.dsf.bpe.v1.ProcessPluginDeploymentStateListener;

public class PingPongDeploymentStateListener implements ProcessPluginDeploymentStateListener, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(PingPongDeploymentStateListener.class);

	private final PingConfig pingConfig;
	private final String networkSpeedUnit;

	public PingPongDeploymentStateListener(PingConfig pingConfig)
	{
		this.pingConfig = pingConfig;
		this.networkSpeedUnit = pingConfig.getNetworkSpeedUnit();
	}

	@Override
	public void onProcessesDeployed(List<String> processes)
	{
		logger.debug("Validating plugin configuration...");
		if (ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUES.contains(networkSpeedUnit))
		{
			logger.debug("Network speed unit is valid: {}", networkSpeedUnit);
		}
		else
		{
			pingConfig.setNetworkSpeedUnit(ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABYTES_PER_SECOND);
			logger.debug("Network speed unit \"{}\" is not valid. Valid values are: {}. Defaulting to \"{}\"",
					networkSpeedUnit, ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUES,
					ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABYTES_PER_SECOND);
		}

		//TODO: fixme
		int maxDownloadSizeBytes = pingConfig.getMaxDownloadSizeBytes();
		int maxDownloadSizeBytesHeapFix = 100000000;
		if (maxDownloadSizeBytes > maxDownloadSizeBytesHeapFix)
		{
			pingConfig.setMaxDownloadSizeBytes(maxDownloadSizeBytesHeapFix);
			logger.debug("MaxDownloadSizeBytes is too large. Setting maxDownloadSizeBytes to {}. This avoids Java running out of memory and will be fixed in a future release", maxDownloadSizeBytesHeapFix);
		}

		int maxUploadSizeBytes = pingConfig.getMaxUploadSizeBytes();
		int maxUploadSizeBytesHeapFix = 100000000;
		if (maxUploadSizeBytes > maxUploadSizeBytesHeapFix)
		{
			pingConfig.setMaxUploadSizeBytes(maxUploadSizeBytesHeapFix);
			logger.debug("MaxUploadSizeBytes is too large. Setting maxUploadSizeBytes to {}. This avoids Java running out of memory and will be fixed in a future release", maxUploadSizeBytesHeapFix);
		}

		logger.debug("Configuration validation complete.");
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(networkSpeedUnit);
		Objects.requireNonNull(pingConfig);
	}
}
