package dev.dsf.bpe.util.task.input.generator;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;

public class ErrorMessageGenerator
{
	public static List<Task.ParameterComponent> create(List<String> errorMessages)
	{
		if (errorMessages == null || errorMessages.isEmpty())
			return List.of();
		return errorMessages.stream().map(ErrorMessageGenerator::create).collect(Collectors.toList());
	}

	public static Task.ParameterComponent create(String errorMessage)
	{
		Task.ParameterComponent param = new Task.ParameterComponent();
		param.setValue(new StringType(errorMessage)).getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING)
				.setCode(ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR_MESSAGE);
		return param;
	}
}
