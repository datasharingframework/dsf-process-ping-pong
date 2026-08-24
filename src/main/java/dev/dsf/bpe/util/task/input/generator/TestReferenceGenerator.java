package dev.dsf.bpe.util.task.input.generator;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;

public final class TestReferenceGenerator
{
	private TestReferenceGenerator()
	{

	}

	public static Task.ParameterComponent create(String uri, String version)
	{
		Reference testReference = new Reference(uri);
		testReference.setType("Binary");
		Task.ParameterComponent testReferenceInput = new Task.ParameterComponent();
		testReferenceInput.setValue(testReference).getType()
				.addCoding(CodeSystem.DsfPing.fromCode(CodeSystem.DsfPing.Code.TEST_REFERENCE, version));

		return testReferenceInput;
	}
}
