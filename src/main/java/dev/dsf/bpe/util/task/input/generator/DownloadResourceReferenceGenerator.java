package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;

public final class DownloadResourceReferenceGenerator
{
	private DownloadResourceReferenceGenerator()
	{
	}

	public static Task.ParameterComponent create(String uri, String resourceVersion)
	{
		Reference reference = new Reference(uri);
		reference.setType("Binary");
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(reference).getType().addCoding(
				CodeSystem.DsfPing.fromCode(CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_REFERENCE, resourceVersion));
		return param;
	}
}
