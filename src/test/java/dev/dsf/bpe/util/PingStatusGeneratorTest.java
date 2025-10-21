package dev.dsf.bpe.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Task;
import org.junit.Test;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.fhir.profiles.TaskProfileTest;

public class PingStatusGeneratorTest
{
	@Test
	public void updatingPongErrorsResultsInOneErrorsExtensionTest()
	{
		String process = "pong";

		List<ProcessError> errors = processErrors(process);

		Task pongTask = TaskProfileTest.createValidTaskStartPingProcess();

		PingStatusGenerator.updatePongStatusOutput(pongTask, errors);

		errors.add(new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_UNEXPECTED, null));

		PingStatusGenerator.updatePongStatusOutput(pongTask, errors);

		List<Extension> errorsExtensions = pongTask.getOutput().stream().map(Element::getExtension)
				.map(extensions -> extensions.stream()
						.filter(extension -> ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_PING_STATUS
								.equals(extension.getUrl()))
						.findFirst().orElse(null))
				.filter(Objects::nonNull)
				.map(extension -> extension.getExtension().stream()
						.filter(extension1 -> ConstantsPing.EXTENSION_URL_ERRORS.equals(extension1.getUrl())).toList())
				.reduce(new ArrayList<>(), (list1, list2) ->
				{
					list1.addAll(list2);
					return list1;
				});

		assertEquals(1, errorsExtensions.size());
	}

	private List<ProcessError> processErrors(String process)
	{
		List<ProcessError> errors = new ArrayList<>();
		errors.add(new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null));
		return errors;
	}
}
