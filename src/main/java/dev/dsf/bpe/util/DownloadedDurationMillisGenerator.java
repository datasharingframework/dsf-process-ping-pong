package dev.dsf.bpe.util;

import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;

public class DownloadedDurationMillisGenerator
{
	public static Task.ParameterComponent create(long durationMillis)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(new DecimalType(durationMillis)).getType().addCoding()
				.setSystem(ConstantsPing.CODESYSTEM_DSF_PING)
				.setCode(ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_DURATION_MILLIS);
		return param;
	}
}
