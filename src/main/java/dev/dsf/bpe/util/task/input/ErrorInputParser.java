package dev.dsf.bpe.util.task.input;

import java.util.List;

import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;

public final class ErrorInputParser
{
	private ErrorInputParser()
	{
	}

	public static List<ProcessError> parseInputs(Task task)
	{
		List<Task.ParameterComponent> inputs = task.getInput().stream()
				.filter(input -> ConstantsPing.CODESYSTEM_DSF_PING_VALUE_ERROR
						.equals(input.getType().getCodingFirstRep().getCode()))
				.toList();

		return inputs.stream()
				.map(input -> ProcessError
						.toError(input.getExtensionByUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR)))
				.toList();
	}
}
