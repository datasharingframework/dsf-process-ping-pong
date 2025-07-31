package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;

public final class DownloadedDurationMillisGenerator
{
	private DownloadedDurationMillisGenerator()
	{
	}

	public static Task.ParameterComponent create(long durationMillis)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(new DecimalType(durationMillis)).getType().addCoding().setSystem(CodeSystem.DsfPing.URL)
				.setCode(CodeSystem.DsfPing.Code.DOWNLOADED_DURATION_MILLIS.getValue());
		return param;
	}
}
