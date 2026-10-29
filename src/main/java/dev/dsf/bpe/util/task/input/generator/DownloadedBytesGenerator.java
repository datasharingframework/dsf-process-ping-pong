package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;

public final class DownloadedBytesGenerator
{
	private DownloadedBytesGenerator()
	{
	}

	public static Task.ParameterComponent create(long bytes, String resourceVersion)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(new DecimalType(bytes)).getType().addCoding().setSystem(CodeSystem.DsfPing.URL)
				.setCode(CodeSystem.DsfPing.Code.DOWNLOADED_BYTES.getValue()).setVersion(resourceVersion);
		return param;
	}
}
