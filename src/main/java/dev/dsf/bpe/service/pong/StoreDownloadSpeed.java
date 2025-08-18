package dev.dsf.bpe.service.pong;

import java.math.BigDecimal;
import java.time.Duration;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.task.NetworkSpeedCalculator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreDownloadSpeed extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(StoreDownloadSpeed.class);
	private final CodeSystem.DsfPingUnits.Code networkSpeedUnit;

	public StoreDownloadSpeed(ProcessPluginApi api, CodeSystem.DsfPingUnits.Code networkSpeedUnit)
	{
		super(api);
		this.networkSpeedUnit = networkSpeedUnit;
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError
	{
		Task startTask = variables.getStartTask();
		logger.debug("Storing download speed...");

		long downloadedBytes = variables.getLong(ExecutionVariables.DOWNLOADED_BYTES.getValue());
		Duration downloadedDuration = (Duration) variables
				.getVariable(ExecutionVariables.DOWNLOADED_DURATION.getValue());

		BigDecimal downloadSpeed = NetworkSpeedCalculator.calculate(downloadedBytes, downloadedDuration,
				networkSpeedUnit);

		PingStatusGenerator.updatePongStatusOutput(startTask, CodeSystem.DsfPingStatus.Code.PENDING);
		PingStatusGenerator.updatePongStatusOutputDownloadSpeed(startTask, downloadSpeed, networkSpeedUnit);

		variables.updateTask(startTask);
		logger.debug("Stored download speed: " + downloadSpeed + " " + networkSpeedUnit);
	}
}
