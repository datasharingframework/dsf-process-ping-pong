package dev.dsf.bpe.util.task.input.generator;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;

public final class ErrorInputComponentGenerator
{
	private ErrorInputComponentGenerator()
	{
	}

	public static List<Task.ParameterComponent> create(List<ProcessError> errors)
	{
		if (errors == null || errors.isEmpty())
			return List.of();
		return errors.stream().map(ErrorInputComponentGenerator::create).collect(Collectors.toList());
	}

	public static Task.ParameterComponent create(ProcessError error)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();

		param.getType().addCoding(
				new Coding(ConstantsPing.CODESYSTEM_DSF_PING, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_ERROR, null));
		param.addExtension(ProcessError.toExtension(error));
		Extension dataAbsentReason = new Extension()
				.setUrl("http://hl7.org/fhir/StructureDefinition/data-absent-reason")
				.setValue(new CodeType("not-applicable"));
		param.setValue(new StringType());
		param.getValue().addExtension(dataAbsentReason);

		return param;
	}
}
