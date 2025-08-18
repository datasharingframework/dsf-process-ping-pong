package dev.dsf.bpe;

import java.io.Serializable;
import java.util.Objects;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UrlType;

//todo: remove process and processStep + CodeSystems, make message never contain e.getMessage() becaus security
// ping status codesystem should extend old ping status codesystem, map every possible error to a unique name in a new codesystem
// that repaces process + processStep, display values should contain the exact text that is now contained in action
// remove equals method
public record ProcessError(CodeSystem.DsfPingProcesses.Code process, CodeSystem.DsfPingProcessSteps.Code processStep,
		String action, String potentialFixUrl, String message) implements Serializable
{
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
				.setValue(new Coding(CodeSystem.DsfPingProcesses.URL, error.process().getValue(), null));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_PROCESS_STEP)
				.setValue(new Coding(CodeSystem.DsfPingProcessSteps.URL, error.process().getValue(), null));
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

		CodeSystem.DsfPingProcesses.Code processCode = CodeSystem.DsfPingProcesses.Code.ofValue(process);
		CodeSystem.DsfPingProcessSteps.Code stepCode = CodeSystem.DsfPingProcessSteps.Code.ofValue(processStep);

		return new ProcessError(processCode, stepCode, action, potentialFixUrl, message);
	}
}
