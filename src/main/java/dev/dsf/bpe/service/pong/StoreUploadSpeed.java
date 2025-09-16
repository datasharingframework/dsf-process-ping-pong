package dev.dsf.bpe.service.pong;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreUploadSpeed extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(StoreUploadSpeed.class);
	private CodeSystem.DsfPingUnits.Code networkSpeedUnit;

	public StoreUploadSpeed(ProcessPluginApi api, CodeSystem.DsfPingUnits.Code networkSpeedUnit)
	{
		super(api);
		this.networkSpeedUnit = networkSpeedUnit;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		Task startTask = variables.getStartTask();
		Task cleanup = variables.getLatestTask();
		logger.debug("Storing upload speed...");

		Optional<DecimalType> uploadedBytesTaskInput = getUploadedBytes(cleanup);
		Optional<org.hl7.fhir.r4.model.Duration> uploadedDurationTaskInput = getUploadedDuration(cleanup);
		long uploadedBytes = uploadedBytesTaskInput.map(PrimitiveType::getValue).orElse(BigDecimal.valueOf(0))
				.longValue();
		Duration uploadedDuration = uploadedDurationTaskInput
				.map(duration -> Duration.ofMillis(duration.getValue().longValue())).orElse(null);

		BigDecimal uploadSpeed = null;
		if (uploadedDuration != null)
		{
			if (Objects.isNull(networkSpeedUnit))
			{
				CodeSystem.DsfPingUnits.Code.SpeedAndUnit speedAndUnit = CodeSystem.DsfPingUnits.Code
						.calculateSpeedWithFittingUnit(uploadedBytes, uploadedDuration);
				uploadSpeed = speedAndUnit.speed();
				networkSpeedUnit = speedAndUnit.unit();
			}
			else
			{
				uploadSpeed = networkSpeedUnit.calculateSpeed(uploadedBytes, uploadedDuration);
			}
		}

		PingStatusGenerator.updatePongStatusOutputUploadSpeed(startTask, uploadSpeed, networkSpeedUnit);

		variables.updateTask(startTask);
		logger.debug("Stored upload speed: " + uploadSpeed + " " + networkSpeedUnit);
	}

	private Optional<DecimalType> getUploadedBytes(Task task)
	{
		return api.getTaskHelper().getFirstInputParameterValue(task, CodeSystem.DsfPing.URL,
				CodeSystem.DsfPing.Code.DOWNLOADED_BYTES.getValue(), DecimalType.class);
	}

	private Optional<org.hl7.fhir.r4.model.Duration> getUploadedDuration(Task task)
	{
		return api.getTaskHelper().getFirstInputParameterValue(task, CodeSystem.DsfPing.URL,
				CodeSystem.DsfPing.Code.DOWNLOADED_DURATION_MILLIS.getValue(), org.hl7.fhir.r4.model.Duration.class);
	}
}
