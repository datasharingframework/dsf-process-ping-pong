package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;

public class DownloadResourceSizeGenerator
{
	public static Task.ParameterComponent create(int sizeBytes)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(new IntegerType(sizeBytes)).getType().addCoding(new Coding(ConstantsPing.CODESYSTEM_DSF_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOAD_RESOURCE_SIZE_BYTES, null));
		return param;
	}
}
