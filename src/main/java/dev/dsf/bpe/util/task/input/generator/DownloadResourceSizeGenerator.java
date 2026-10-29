package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;

public final class DownloadResourceSizeGenerator
{
	private DownloadResourceSizeGenerator()
	{
	}

	public static Task.ParameterComponent create(long sizeBytes, String resourceVersion)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(new DecimalType(sizeBytes)).getType().addCoding(
				CodeSystem.DsfPing.fromCode(CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_SIZE_BYTES, resourceVersion));
		return param;
	}
}
