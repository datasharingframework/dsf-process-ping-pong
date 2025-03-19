package dev.dsf.bpe.service.pong;

import java.math.BigDecimal;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.NetworkSpeedCalculator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreUploadSpeed extends AbstractServiceDelegate
{
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
		Task cleanup = variables.getLatestTask();
		PingPongLogger logger = new PingPongLogger(LogPing.class, startTask);
		logger.debug("Storing upload speed...");

		Optional<IntegerType> uploadedBytesTaskInput = getUploadedBytes(cleanup);
		Optional<DecimalType> uploadedDurationMillisTaskInput = getUploadedDurationMillis(cleanup);
		int uploadedBytes = uploadedBytesTaskInput.map(PrimitiveType::getValue).orElse(0);
		long uploadedDurationMillis = uploadedDurationMillisTaskInput
				.map(decimalType -> decimalType.getValue().longValue()).orElse(0L);

		BigDecimal uploadSpeed = NetworkSpeedCalculator.calculate(uploadedBytes, uploadedDurationMillis,
				networkSpeedUnit);

		PingStatusGenerator.updatePongStatusOutputUploadSpeed(startTask, uploadSpeed, networkSpeedUnit);

		variables.updateTask(startTask);
		logger.debug("Stored upload speed: " + uploadSpeed + " " + networkSpeedUnit);
	}

	private Optional<IntegerType> getUploadedBytes(Task task)
	{
		return api.getTaskHelper().getFirstInputParameterValue(task, ConstantsPing.CODESYSTEM_DSF_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_BYTES, IntegerType.class);
	}

	private Optional<DecimalType> getUploadedDurationMillis(Task task)
	{
		return api.getTaskHelper().getFirstInputParameterValue(task, ConstantsPing.CODESYSTEM_DSF_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_DURATION_MILLIS, DecimalType.class);
	}
}
