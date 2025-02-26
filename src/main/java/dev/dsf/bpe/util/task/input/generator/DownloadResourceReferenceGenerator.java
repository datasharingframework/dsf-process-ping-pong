package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;

public class DownloadResourceReferenceGenerator
{
	public static Task.ParameterComponent create(String uri)
	{
		Reference reference = new Reference(uri);
		reference.setType("Binary");
		return create(reference);
	}

	public static Task.ParameterComponent create(Reference reference)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(reference).getType().addCoding(new Coding(ConstantsPing.CODESYSTEM_DSF_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOAD_RESOURCE_REFERENCE, null));
		return param;
	}
}
