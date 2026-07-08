package dev.dsf.bpe.service.pong;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class StoreDownloadSpeed implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(StoreDownloadSpeed.class);
	private final PingStatusGenerator pingStatusGenerator;
	private CodeSystem.DsfPingUnits.Code networkSpeedUnit;

	public StoreDownloadSpeed(CodeSystem.DsfPingUnits.Code networkSpeedUnit, PingStatusGenerator pingStatusGenerator)
	{
		this.networkSpeedUnit = networkSpeedUnit;
		this.pingStatusGenerator = pingStatusGenerator;
	}

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Task startTask = variables.getStartTask();
		logger.debug("Storing download speed...");

		long downloadedBytes = variables.getLong(ExecutionVariables.downloadedBytes.name());
		Duration downloadedDuration = variables.getVariable(ExecutionVariables.downloadedDuration.name());

		BigDecimal downloadSpeed = null;
		if (downloadedDuration != null)
		{
			if (Objects.isNull(networkSpeedUnit))
			{
				CodeSystem.DsfPingUnits.Code.SpeedAndUnit speedAndUnit = CodeSystem.DsfPingUnits.Code
						.calculateSpeedWithFittingUnit(downloadedBytes, downloadedDuration);
				downloadSpeed = speedAndUnit.speed();
				networkSpeedUnit = speedAndUnit.unit();
			}
			else
			{
				downloadSpeed = networkSpeedUnit.calculateSpeed(downloadedBytes, downloadedDuration);
			}
		}

		pingStatusGenerator.updatePongStatusOutputDownloadSpeed(startTask, downloadSpeed, networkSpeedUnit);

		variables.updateTask(startTask);
		logger.debug("Stored download speed: " + downloadSpeed + " " + networkSpeedUnit);
	}
}
