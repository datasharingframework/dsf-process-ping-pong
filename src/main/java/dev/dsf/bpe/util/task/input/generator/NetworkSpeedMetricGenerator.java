package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;

public class NetworkSpeedMetricGenerator
{
	public static Task.ParameterComponent createDownloadedDurationMillis(long duration)
	{
		Task.ParameterComponent downloadedDuration = new Task.ParameterComponent();
		downloadedDuration.setValue(new DecimalType(duration)).getType()
				.addCoding(new Coding(ConstantsPing.CODESYSTEM_DSF_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_DURATION_MILLIS, null));
		return downloadedDuration;
	}

	public static Task.ParameterComponent createDownloadedBytes(int bytes)
	{
		Task.ParameterComponent downloadedBytes = new Task.ParameterComponent();
		downloadedBytes.setValue(new IntegerType(bytes)).getType().addCoding(new Coding(
				ConstantsPing.CODESYSTEM_DSF_PING, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_BYTES, null));
		return downloadedBytes;
	}
}
