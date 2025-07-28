package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;

public final class DownloadedBytesGenerator
{
	private DownloadedBytesGenerator()
	{
	}

	public static Task.ParameterComponent create(long bytes)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(new DecimalType(bytes)).getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING)
				.setCode(ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_BYTES);
		return param;
	}
}
