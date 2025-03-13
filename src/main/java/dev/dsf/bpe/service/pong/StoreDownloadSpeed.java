package dev.dsf.bpe.service.pong;

import java.math.BigDecimal;
import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.task.NetworkSpeedCalculator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreDownloadSpeed extends AbstractServiceDelegate
{
	private final String networkSpeedUnit;

	public StoreDownloadSpeed(ProcessPluginApi api, String networkSpeedUnit)
	{
		super(api);
		this.networkSpeedUnit = networkSpeedUnit;
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Task startTask = variables.getStartTask();

		int downloadedBytes = variables.getInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes());
		long downloadedDurationMillis = variables
				.getLong(ConstantsPing.getBpmnExecutionVariableUploadedDurationMillis());

		BigDecimal downloadSpeed = NetworkSpeedCalculator.calculate(downloadedBytes, downloadedDurationMillis,
				networkSpeedUnit);

		PingStatusGenerator.updatePongStatusOutput(startTask, ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_RESOURCE_DOWNLOADED);
		PingStatusGenerator.updateStatusOutputDownloadSpeed(startTask, downloadSpeed, networkSpeedUnit);

		variables.updateTask(startTask);
	}
}
