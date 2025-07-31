package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;

public final class DownloadResourceSizeGenerator
{
	private DownloadResourceSizeGenerator()
	{
	}

	public static Task.ParameterComponent create(long sizeBytes)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(new DecimalType(sizeBytes)).getType().addCoding(new Coding(CodeSystem.DsfPing.URL,
				CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue(), null));
		return param;
	}
}
