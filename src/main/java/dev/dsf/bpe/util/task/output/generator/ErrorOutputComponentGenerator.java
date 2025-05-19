package dev.dsf.bpe.util.task.output.generator;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;

public class ErrorOutputComponentGenerator
{
	public static List<Task.TaskOutputComponent> create(List<ProcessError> errors)
	{
		if (errors == null || errors.isEmpty())
			return List.of();
		return errors.stream().map(ErrorOutputComponentGenerator::create).collect(Collectors.toList());
	}

	public static Task.TaskOutputComponent create(ProcessError error)
	{
		Task.TaskOutputComponent param = new Task.TaskOutputComponent();

		param.getType().addCoding(
				new Coding(ConstantsPing.CODESYSTEM_DSF_PING, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_ERROR, null));
		param.addExtension(ProcessError.toOutputExtension(error));
		Extension dataAbsentReason = new Extension()
				.setUrl("http://hl7.org/fhir/StructureDefinition/data-absent-reason").setValue(
						new Coding("http://terminology.hl7.org/CodeSystem/data-absent-reason", "not-applicable", null));
		param.addExtension(dataAbsentReason);

		return param;
	}
}
