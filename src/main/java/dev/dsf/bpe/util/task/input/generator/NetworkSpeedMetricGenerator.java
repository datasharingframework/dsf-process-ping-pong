package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;

public final class NetworkSpeedMetricGenerator
{
	private NetworkSpeedMetricGenerator()
	{
	}

	public static Task.ParameterComponent createDownloadedDurationMillis(long duration)
	{
		Task.ParameterComponent downloadedDuration = new Task.ParameterComponent();
		downloadedDuration.setValue(new DecimalType(duration)).getType().addCoding(new Coding(CodeSystem.DsfPing.URL,
				CodeSystem.DsfPing.Code.DOWNLOADED_DURATION_MILLIS.getValue(), null));
		return downloadedDuration;
	}

	public static Task.ParameterComponent createDownloadedBytes(long bytes)
	{
		Task.ParameterComponent downloadedBytes = new Task.ParameterComponent();
		downloadedBytes.setValue(new DecimalType(bytes)).getType().addCoding(
				new Coding(CodeSystem.DsfPing.URL, CodeSystem.DsfPing.Code.DOWNLOADED_BYTES.getValue(), null));
		return downloadedBytes;
	}
}
