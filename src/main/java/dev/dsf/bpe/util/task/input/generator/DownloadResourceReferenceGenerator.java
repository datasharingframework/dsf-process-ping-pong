package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.PingProcessPluginDefinition;

public final class DownloadResourceReferenceGenerator
{
	private DownloadResourceReferenceGenerator()
	{
	}

	public static Task.ParameterComponent create(String uri)
	{
		Reference reference = new Reference(uri);
		reference.setType("Binary");
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(reference).getType()
				.addCoding(CodeSystem.DsfPing.fromCode(CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_REFERENCE));
		return param;
	}
}
