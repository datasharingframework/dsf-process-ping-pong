package dev.dsf.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UrlType;
import org.junit.Test;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;

public class ProcessErrorTest
{
	private static final String testString = "foo";
	private static final String testProcess = ConstantsPing.PROCESS_NAME_PING;
	private static final CodeSystem.DsfPingError.Concept testConcept = CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_401;

	@Test
	public void ExtensionToErrorTest()
	{
		ProcessError expected = new ProcessError(testProcess, testConcept, testString);
		assertEquals(expected, ProcessError.toError(getExtensionFull(), testProcess));
	}

	@Test
	public void ExtensionWithoutFixUrlToErrorTest()
	{
		ProcessError expected = new ProcessError(testProcess, testConcept, null);
		assertEquals(expected, ProcessError.toError(getExtensionMissingFixUrl(), testProcess));
	}

	@Test
	public void ExtensionWithoutErrorToErrorTest()
	{
		assertThrows(NullPointerException.class, () -> ProcessError.toError(getExtensionMissingError(), testProcess));
	}


	private Extension getExtensionFull()
	{
		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ERROR)
				.setValue(new Coding().setSystem(CodeSystem.DsfPingError.URL).setCode(testConcept.getCode())
						.setDisplay(testConcept.getDisplay()));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX).setValue(new UrlType(testString));

		return extension;
	}

	private Extension getExtensionMissingFixUrl()
	{
		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ERROR)
				.setValue(new Coding().setSystem(CodeSystem.DsfPingError.URL).setCode(testConcept.getCode())
						.setDisplay(testConcept.getDisplay()));

		return extension;
	}

	private Extension getExtensionMissingError()
	{
		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX).setValue(new UrlType(testString));

		return extension;
	}
}
