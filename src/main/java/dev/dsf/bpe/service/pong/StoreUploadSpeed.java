package dev.dsf.bpe.service.pong;

import java.math.BigDecimal;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.task.NetworkSpeedCalculator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreUploadSpeed extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(StoreUploadSpeed.class);
	private final String networkSpeedUnit;

	public StoreUploadSpeed(ProcessPluginApi api, String networkSpeedUnit)
	{
		super(api);
		this.networkSpeedUnit = networkSpeedUnit;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		Task startTask = variables.getStartTask();

		int uploadedBytes = variables.getInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes());
		long uploadedDurationMillis = variables.getLong(ConstantsPing.getBpmnExecutionVariableUploadedDurationMillis());

		BigDecimal uploadSpeed = NetworkSpeedCalculator.calculate(uploadedBytes, uploadedDurationMillis,
				networkSpeedUnit);

		PingStatusGenerator.updateStatusOutputDownloadSpeed(startTask, uploadSpeed, networkSpeedUnit);

		variables.updateTask(startTask);
	}
}
