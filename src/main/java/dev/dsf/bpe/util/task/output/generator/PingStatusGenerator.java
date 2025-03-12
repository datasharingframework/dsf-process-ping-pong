package dev.dsf.bpe.util.task.output.generator;

import java.util.List;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.constants.NamingSystems.EndpointIdentifier;
import dev.dsf.bpe.v1.constants.NamingSystems.OrganizationIdentifier;
import dev.dsf.bpe.v1.variables.Target;

public class PingStatusGenerator
{
	public static TaskOutputComponent createPingStatusOutput(Target target, String statusCode)
	{
		return createPingStatusOutput(target, statusCode, null);
	}

	public static TaskOutputComponent createPingStatusOutput(Target target, String statusCode, int downloadSpeed,
			int uploadSpeed, String unit)
	{
		return createPingStatusOutput(target, statusCode, null, downloadSpeed, uploadSpeed, unit);
	}

	public static TaskOutputComponent createPingStatusOutput(Target target, String statusCode,
			List<String> errorMessages)
	{
		return createStatusOutput(target, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS, statusCode,
				errorMessages, -1, -1, null);
	}

	public static TaskOutputComponent createPingStatusOutput(Target target, String statusCode,
			List<String> errorMessages, int downloadSpeed, int uploadSpeed, String unit)
	{
		return createStatusOutput(target, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS, statusCode,
				errorMessages, downloadSpeed, uploadSpeed, unit);
	}

	public static TaskOutputComponent createPongStatusOutput(Target target, String statusCode)
	{
		return createPongStatusOutput(target, statusCode, null);
	}

	public static TaskOutputComponent createPongStatusOutput(Target target, String statusCode, int downloadSpeed,
			int uploadSpeed, String unit)
	{
		return createPongStatusOutput(target, statusCode, null, downloadSpeed, uploadSpeed, unit);
	}

	public static TaskOutputComponent createPongStatusOutput(Target target, String statusCode,
			List<String> errorMessages)
	{
		return createStatusOutput(target, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS, statusCode,
				errorMessages, -1, -1, null);
	}

	public static TaskOutputComponent createPongStatusOutput(Target target, String statusCode,
			List<String> errorMessages, int downloadSpeed, int uploadSpeed, String unit)
	{
		return createStatusOutput(target, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS, statusCode,
				errorMessages, downloadSpeed, uploadSpeed, unit);
	}

	private static TaskOutputComponent createStatusOutput(Target target, String outputParameter, String statusCode,
			List<String> errorMessages, int downloadSpeed, int uploadSpeed, String unit)
	{
		TaskOutputComponent output = new TaskOutputComponent();
		output.setValue(new Coding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING_STATUS).setCode(statusCode));
		output.getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING).setCode(outputParameter);

		Extension extension = output.addExtension();
		extension.setUrl(ConstantsPing.EXTENSION_URL_PING_STATUS);
		extension.addExtension(ConstantsPing.EXTENSION_URL_CORRELATION_KEY, new StringType(target.getCorrelationKey()));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER)
				.setValue(OrganizationIdentifier.withValue(target.getOrganizationIdentifierValue()));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER)
				.setValue(EndpointIdentifier.withValue(target.getEndpointIdentifierValue()));
		if (errorMessages != null)
			for (String errorMessage : errorMessages)
			{
				extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ERROR_MESSAGE)
						.setValue(new StringType(errorMessage));
			}

		if (downloadSpeed >= 0 && unit != null)
		{
			Extension downloadSpeedExtension = extension.addExtension()
					.setUrl(ConstantsPing.EXTENSION_URL_DOWNLOAD_SPEED);
			Extension networkSpeed = downloadSpeedExtension.addExtension()
					.setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED);
			networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_VALUE)
					.setValue(new DecimalType(downloadSpeed));
			networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_UNIT)
					.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_UNITS, unit, null));
		}

		if (uploadSpeed >= 0 && unit != null)
		{
			Extension uploadSpeedExtension = extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_UPLOAD_SPEED);
			Extension networkSpeed = uploadSpeedExtension.addExtension()
					.setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED);
			networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_VALUE)
					.setValue(new DecimalType(downloadSpeed));
			networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_UNIT)
					.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_UNITS, unit, null));
		}

		return output;
	}
}
