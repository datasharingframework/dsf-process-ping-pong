package dev.dsf.bpe;

import java.io.Serializable;
import java.util.List;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UrlType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public record ProcessError(String process, String processStep, String action, String potentialFixUrl, String message)
		implements Serializable
{
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof ProcessError error && process.equals(error.process())
				&& processStep.equals(error.processStep()) && action.equals(error.action())
				&& message.equals(error.message());

	}

	public static Extension toExtension(ProcessError error)
	{
		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES, error.process(), null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS_STEP)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS, error.process(), null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ACTION).setValue(new StringType(error.action()));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX)
				.setValue(new UrlType(error.potentialFixUrl()));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_MESSAGE).setValue(new StringType(error.message()));

		return extension;
	}

	public static ProcessError toError(Extension extension)
	{
		String process = ((Coding) extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_PROCESS).getValue())
				.getCode();
		String processStep = ((Coding) extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_PROCESS_STEP).getValue())
				.getCode();
		String action = ((StringType) extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_ACTION).getValue())
				.getValue();
		String potentialFixUrl = ((UrlType) extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX)
				.getValue()).getValue();
		String message = ((StringType) extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_MESSAGE).getValue())
				.getValue();

		return new ProcessError(process, processStep, action, potentialFixUrl, message);
	}

	public static String toString(List<ProcessError> errors) throws JsonProcessingException
	{
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(errors);
	}

	public static String toString(ProcessError error) throws JsonProcessingException
	{
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(error);
	}

	public static List<ProcessError> parseList(String json) throws JsonProcessingException
	{
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, new TypeReference<List<ProcessError>>()
		{
		});
	}

	public static ProcessError parse(String json) throws JsonProcessingException
	{
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, ProcessError.class);
	}
}
