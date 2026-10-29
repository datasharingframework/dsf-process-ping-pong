package dev.dsf.bpe.util.task.input.generator;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ProcessError;

public final class ErrorInputComponentGenerator
{
	private ErrorInputComponentGenerator()
	{
	}

	public static List<Task.ParameterComponent> create(List<ProcessError> errors, String resourceVersion)
	{
		if (errors == null || errors.isEmpty())
			return List.of();
		return errors.stream().map(e -> ErrorInputComponentGenerator.create(e, resourceVersion))
				.collect(Collectors.toList());
	}

	public static Task.ParameterComponent create(ProcessError error, String resourceVersion)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();

		param.getType().addCoding(CodeSystem.DsfPing.fromCode(CodeSystem.DsfPing.Code.ERROR, resourceVersion));
		param.addExtension(ProcessError.toExtension(error, resourceVersion));
		Extension dataAbsentReason = new Extension()
				.setUrl("http://hl7.org/fhir/StructureDefinition/data-absent-reason")
				.setValue(new CodeType("not-applicable"));
		param.setValue(new StringType());
		param.getValue().addExtension(dataAbsentReason);

		return param;
	}
}
