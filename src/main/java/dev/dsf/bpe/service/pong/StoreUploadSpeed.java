package dev.dsf.bpe.service.pong;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class StoreUploadSpeed implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(StoreUploadSpeed.class);
	private final PingStatusGenerator pingStatusGenerator;
	private CodeSystem.DsfPingUnits.Code networkSpeedUnit;

	public StoreUploadSpeed(CodeSystem.DsfPingUnits.Code networkSpeedUnit, PingStatusGenerator pingStatusGenerator)
	{
		this.networkSpeedUnit = networkSpeedUnit;
		this.pingStatusGenerator = pingStatusGenerator;
	}

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Task startTask = variables.getStartTask();
		Task cleanup = variables.getLatestTask();
		logger.debug("Storing upload speed...");

		Optional<DecimalType> uploadedBytesTaskInput = getUploadedBytes(api, cleanup);
		Optional<org.hl7.fhir.r4.model.Duration> uploadedDurationTaskInput = getUploadedDuration(api, cleanup);
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

		pingStatusGenerator.updatePongStatusOutputUploadSpeed(startTask, uploadSpeed, networkSpeedUnit);

		variables.updateTask(startTask);
		logger.debug("Stored upload speed: " + uploadSpeed + " " + networkSpeedUnit);
	}

	private Optional<DecimalType> getUploadedBytes(ProcessPluginApi api, Task task)
	{
		return api.getTaskHelper().getFirstInputParameterValue(task, CodeSystem.DsfPing.URL,
				CodeSystem.DsfPing.Code.DOWNLOADED_BYTES.getValue(), DecimalType.class);
	}

	private Optional<org.hl7.fhir.r4.model.Duration> getUploadedDuration(ProcessPluginApi api, Task task)
	{
		return api.getTaskHelper().getFirstInputParameterValue(task, CodeSystem.DsfPing.URL,
				CodeSystem.DsfPing.Code.DOWNLOADED_DURATION_MILLIS.getValue(), org.hl7.fhir.r4.model.Duration.class);
	}
}
