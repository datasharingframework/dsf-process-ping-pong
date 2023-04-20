package dev.dsf.bpe.util;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;

import dev.dsf.bpe.ConstantsBase;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.fhir.variables.Target;

public class PingStatusGenerator
{
	public TaskOutputComponent createPingStatusOutput(Target target, String statusCode)
	{
		return createPingStatusOutput(target, statusCode, null);
	}

	public TaskOutputComponent createPingStatusOutput(Target target, String statusCode, String errorMessage)
	{
		return createStatusOutput(target, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS, statusCode,
				errorMessage);
	}

	public TaskOutputComponent createPongStatusOutput(Target target, String statusCode)
	{
		return createPongStatusOutput(target, statusCode, null);
	}

	public TaskOutputComponent createPongStatusOutput(Target target, String statusCode, String errorMessage)
	{
		return createStatusOutput(target, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS, statusCode,
				errorMessage);
	}

	private TaskOutputComponent createStatusOutput(Target target, String outputParameter, String statusCode,
			String errorMessage)
	{
		TaskOutputComponent output = new TaskOutputComponent();
		output.setValue(new Coding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING_STATUS).setCode(statusCode));
		output.getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING).setCode(outputParameter);

		Extension extension = output.addExtension();
		extension.setUrl(ConstantsPing.EXTENSION_URL_PING_STATUS);
		extension.addExtension(ConstantsPing.EXTENSION_URL_CORRELATION_KEY, new StringType(target.getCorrelationKey()));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER)
				.setValue(new Identifier().setSystem(ConstantsBase.NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER)
						.setValue(target.getOrganizationIdentifierValue()));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER)
				.setValue(new Identifier().setSystem(ConstantsBase.NAMINGSYSTEM_DSF_ENDPOINT_IDENTIFIER)
						.setValue(target.getEndpointIdentifierValue()));
		if (errorMessage != null)
			extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ERROR_MESSAGE)
					.setValue(new StringType(errorMessage));

		return output;
	}
}
