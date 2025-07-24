package dev.dsf.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UrlType;
import org.junit.Test;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;

public class ProcessErrorTest
{
	private static final String testString = "foo";

	@Test
	public void ExtensionToErrorTest()
	{
		ProcessError expected = new ProcessError(testString, testString, testString, testString, testString);
		assertEquals(expected, ProcessError.toError(getExtensionFull()));
	}

	@Test
	public void ExtensionWithoutFixUrlToErrorTest()
	{
		ProcessError expected = new ProcessError(testString, testString, testString, null, testString);
		assertEquals(expected, ProcessError.toError(getExtensionMissingFixUrl()));
	}

	@Test
	public void ExtensionWithoutProcessToErrorTest()
	{
		assertThrows(NullPointerException.class, () -> ProcessError.toError(getExtensionMissingProcess()));
	}

	@Test
	public void ExtensionWithoutProcessStepToErrorTest()
	{
		assertThrows(NullPointerException.class, () -> ProcessError.toError(getExtensionMissingProcessStep()));
	}

	@Test
	public void ExtensionWithoutActionToErrorTest()
	{
		assertThrows(NullPointerException.class, () -> ProcessError.toError(getExtensionMissingAction()));
	}

	@Test
	public void ExtensionWithoutMessageToErrorTest()
	{
		assertThrows(NullPointerException.class, () -> ProcessError.toError(getExtensionMissingMessage()));
	}


	private Extension getExtensionFull()
	{
		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES, testString, null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS_STEP)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS, testString, null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ACTION).setValue(new StringType(testString));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX).setValue(new UrlType(testString));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_MESSAGE).setValue(new StringType(testString));

		return extension;
	}

	private Extension getExtensionMissingFixUrl()
	{
		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES, testString, null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS_STEP)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS, testString, null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ACTION).setValue(new StringType(testString));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_MESSAGE).setValue(new StringType(testString));

		return extension;
	}

	private Extension getExtensionMissingProcess()
	{
		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS_STEP)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS, testString, null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ACTION).setValue(new StringType(testString));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX).setValue(new UrlType(testString));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_MESSAGE).setValue(new StringType(testString));

		return extension;
	}

	private Extension getExtensionMissingProcessStep()
	{
		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES, testString, null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ACTION).setValue(new StringType(testString));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX).setValue(new UrlType(testString));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_MESSAGE).setValue(new StringType(testString));

		return extension;
	}

	private Extension getExtensionMissingAction()
	{
		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES, testString, null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS_STEP)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS, testString, null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX).setValue(new UrlType(testString));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_MESSAGE).setValue(new StringType(testString));

		return extension;
	}

	private Extension getExtensionMissingMessage()
	{
		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES, testString, null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS_STEP)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS, testString, null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ACTION).setValue(new StringType(testString));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX).setValue(new UrlType(testString));

		return extension;
	}
}
