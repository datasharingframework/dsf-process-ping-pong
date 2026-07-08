package dev.dsf.bpe;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UrlType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record ProcessError(String process, CodeSystem.DsfPingError.Concept concept, String potentialFixUrl)
		implements Serializable
{
	public static Extension toExtension(ProcessError error, String resourceVersion)
	{
		Objects.requireNonNull(error);
		Objects.requireNonNull(error.concept());
		Objects.requireNonNull(error.process());

		Extension extension = new Extension();
		extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_ERROR);

		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ERROR)
				.setValue(CodeSystem.DsfPingError.fromConcept(error.concept(), resourceVersion));
		if (Objects.nonNull(error.potentialFixUrl))
		{
			extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX)
					.setValue(new UrlType(error.potentialFixUrl()));
		}

		return extension;
	}

	public static ProcessError toError(Extension extension, String process)
	{
		Extension errorExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_ERROR);
		Objects.requireNonNull(errorExtension);
		CodeSystem.DsfPingError.Concept error = CodeSystem.DsfPingError.Concept
				.fromCode(((Coding) errorExtension.getValue()).getCode());

		Extension potentalFixUrlExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_POTENTIAL_FIX);
		String potentialFixUrl = Objects.nonNull(potentalFixUrlExtension)
				? ((UrlType) potentalFixUrlExtension.getValue()).getValue()
				: null;

		return new ProcessError(process, error, potentialFixUrl);
	}

	public static List<Task.TaskOutputComponent> toTaskOutput(List<ProcessError> errors, String resourceVersion)
	{
		if (errors == null || errors.isEmpty())
			return List.of();
		return errors.stream().map(e -> toTaskOutput(e, resourceVersion)).collect(Collectors.toList());
	}

	public static Task.TaskOutputComponent toTaskOutput(ProcessError error, String resourceVersion)
	{
		Task.TaskOutputComponent param = new Task.TaskOutputComponent();

		param.getType().addCoding(CodeSystem.DsfPing.fromCode(CodeSystem.DsfPing.Code.ERROR, resourceVersion));
		param.addExtension(ProcessError.toExtension(error, resourceVersion));
		Extension dataAbsentReason = new Extension()
				.setUrl("http://hl7.org/fhir/StructureDefinition/data-absent-reason")
				.setValue(new CodeType("not-applicable"));
		param.setValue(new StringType());
		param.getValue().addExtension(dataAbsentReason);

		return param;
	}
}
