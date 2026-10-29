package dev.dsf.bpe.util.task.input.generator;

import java.time.Duration;

import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;

public final class DownloadedDurationGenerator
{
	private static final String CODESYSTEM_UCUM = "http://unitsofmeasure.org";
	private static final String CODESYSTEM_UCUM_CODE_MILLISECONDS = "ms";

	private DownloadedDurationGenerator()
	{
	}

	public static Task.ParameterComponent create(Duration duration, String resourceVersion)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(new org.hl7.fhir.r4.model.Duration().setValue(duration.toMillis()).setSystem(CODESYSTEM_UCUM)
				.setCode(CODESYSTEM_UCUM_CODE_MILLISECONDS)).getType().addCoding().setSystem(CodeSystem.DsfPing.URL)
				.setCode(CodeSystem.DsfPing.Code.DOWNLOADED_DURATION_MILLIS.getValue()).setVersion(resourceVersion);
		return param;
	}
}
