package dev.dsf.bpe;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

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
		Objects.requireNonNull(error);
		Objects.requireNonNull(error.action());
		Objects.requireNonNull(error.process());
		Objects.requireNonNull(error.message());
		Objects.requireNonNull(error.processStep());

		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES, error.process(), null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS_STEP)
				.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS, error.process(), null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ACTION).setValue(new StringType(error.action()));
		if (Objects.nonNull(error.potentialFixUrl))
		{
			extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX)
					.setValue(new UrlType(error.potentialFixUrl()));
		}
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_MESSAGE).setValue(new StringType(error.message()));

		return extension;
	}

	public static ProcessError toError(Extension extension)
	{
		Extension processExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_PROCESS);
		Objects.requireNonNull(processExtension);
		String process = ((Coding) processExtension.getValue()).getCode();


		Extension processStepExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_PROCESS_STEP);
		Objects.requireNonNull(processStepExtension);
		String processStep = ((Coding) processStepExtension.getValue()).getCode();

		Extension actionExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_ACTION);
		Objects.requireNonNull(actionExtension);
		String action = ((StringType) actionExtension.getValue()).getValue();

		Extension potentalFixUrlExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX);
		String potentialFixUrl = Objects.nonNull(potentalFixUrlExtension)
				? ((UrlType) potentalFixUrlExtension.getValue()).getValue()
				: null;

		Extension messageExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_MESSAGE);
		Objects.requireNonNull(messageExtension);
		String message = ((StringType) messageExtension.getValue()).getValue();

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
