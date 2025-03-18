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
		logger.debug("Configuration validation complete.");
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(networkSpeedUnit);
	}
}
