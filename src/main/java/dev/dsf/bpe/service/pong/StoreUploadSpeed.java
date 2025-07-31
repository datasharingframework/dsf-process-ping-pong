package dev.dsf.bpe.service.pong;

import java.math.BigDecimal;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.NetworkSpeedCalculator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreUploadSpeed extends AbstractServiceDelegate
{
	private final CodeSystem.DsfPingUnits.Code networkSpeedUnit;

	public StoreUploadSpeed(ProcessPluginApi api, CodeSystem.DsfPingUnits.Code networkSpeedUnit)
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

		Optional<DecimalType> uploadedBytesTaskInput = getUploadedBytes(cleanup);
		Optional<DecimalType> uploadedDurationMillisTaskInput = getUploadedDurationMillis(cleanup);
		long uploadedBytes = uploadedBytesTaskInput.map(PrimitiveType::getValue).orElse(BigDecimal.valueOf(0))
				.longValue();
		long uploadedDurationMillis = uploadedDurationMillisTaskInput
				.map(decimalType -> decimalType.getValue().longValue()).orElse(0L);

		BigDecimal uploadSpeed = NetworkSpeedCalculator.calculate(uploadedBytes, uploadedDurationMillis,
				networkSpeedUnit);

		PingStatusGenerator.updatePongStatusOutputUploadSpeed(startTask, uploadSpeed, networkSpeedUnit);

		variables.updateTask(startTask);
		logger.debug("Stored upload speed: " + uploadSpeed + " " + networkSpeedUnit);
	}

	private Optional<DecimalType> getUploadedBytes(Task task)
	{
		return api.getTaskHelper().getFirstInputParameterValue(task, CodeSystem.DsfPing.URL,
				CodeSystem.DsfPing.Code.DOWNLOADED_BYTES.getValue(), DecimalType.class);
	}

	private Optional<DecimalType> getUploadedDurationMillis(Task task)
	{
		return api.getTaskHelper().getFirstInputParameterValue(task, CodeSystem.DsfPing.URL,
				CodeSystem.DsfPing.Code.DOWNLOADED_DURATION_MILLIS.getValue(), DecimalType.class);
	}
}
