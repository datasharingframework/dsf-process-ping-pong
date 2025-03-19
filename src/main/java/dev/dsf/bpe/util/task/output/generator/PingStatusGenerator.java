package dev.dsf.bpe.util.task.output.generator;

import static dev.dsf.bpe.util.VersionUtils.appendFhirResourceVersion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.Type;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.constants.NamingSystems.EndpointIdentifier;
import dev.dsf.bpe.v1.constants.NamingSystems.OrganizationIdentifier;
import dev.dsf.bpe.v1.variables.Target;

public class PingStatusGenerator
{
	public static Task updatePingStatusOutput(Task task, String correlationKey, List<String> errorMessages)
	{
		List<TaskOutputComponent> outputs = filterByCorrelationKey(
				getOutputsByExtensionUrlAndCodes(task, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS),
				correlationKey);

		if (outputs.isEmpty())
		{
			task.addOutput(updateStatusOutput(new TaskOutputComponent(), errorMessages));
		}
		else
		{
			if (outputs.size() == 1)
			{
				updateStatusOutput(outputs.get(0), errorMessages);
			}
			else
			{
				throw new RuntimeException("There is more than one ping/pong status output for task " + task.getId()
						+ " with correlation key " + correlationKey);
			}
		}
		return task;
	}

	public static Task updatePongStatusOutput(Task task, List<String> errorMessages)
	{
		List<Task.TaskOutputComponent> pongStatusOutputs = getOutputsByExtensionUrlAndCodes(task,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS);
		if (pongStatusOutputs.isEmpty())
		{
			task.addOutput(updateStatusOutput(new TaskOutputComponent(), errorMessages));
		}
		else
		{
			if (pongStatusOutputs.size() == 1)
			{
				updateStatusOutput(pongStatusOutputs.get(0), errorMessages);
			}
			else
			{
				throw new RuntimeException("There is more than one pong status output for task " + task.getId());
			}
		}

		return task;
	}

	public static TaskOutputComponent updateStatusOutput(TaskOutputComponent output, List<String> errorMessages)
	{
		if (output != null)
		{
			Extension pingStatusExtension = getOrCreatePingStatusExtension(output);
			List<Extension> errorMessageExtensions = pingStatusExtension.getExtension().stream()
					.filter(extension -> ConstantsPing.EXTENSION_URL_ERROR_MESSAGE.equals(extension.getUrl())).toList();
			if (errorMessageExtensions.isEmpty())
			{
				addErrorMessages(output, errorMessages);
			}
			else
			{
				Set<String> existingErrors = errorMessageExtensions.stream()
						.map(extension -> ((StringType) extension.getValue()).getValue()).collect(Collectors.toSet());
				existingErrors.addAll(errorMessages);
				List<Extension> nonErrorMessageExtensions = pingStatusExtension.getExtension().stream()
						.filter(extension -> !ConstantsPing.EXTENSION_URL_ERROR_MESSAGE.equals(extension.getUrl()))
						.toList();
				List<Extension> updatedErrorMessageExtensions = existingErrors.stream()
						.map(errorMessage -> new Extension().setUrl(ConstantsPing.EXTENSION_URL_ERROR_MESSAGE)
								.setValue(new StringType(errorMessage)))
						.collect(Collectors.toCollection(ArrayList::new));
				updatedErrorMessageExtensions.addAll(nonErrorMessageExtensions);
				pingStatusExtension.setExtension(updatedErrorMessageExtensions);
			}
		}

		return output;
	}

	public static Task updatePingStatusOutput(Task task, String correlationKey, String statusCode)
	{
		List<Task.TaskOutputComponent> pingStatusOutputs = filterByCorrelationKey(
				getOutputsByExtensionUrlAndCodes(task, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS),
				correlationKey);
		if (pingStatusOutputs.isEmpty())
		{
			task.addOutput(updatePingStatusOutput(new TaskOutputComponent(), statusCode));
		}
		else
		{
			if (pingStatusOutputs.size() == 1)
			{
				updatePingStatusOutput(pingStatusOutputs.get(0), statusCode);
			}
			else
			{
				throw new RuntimeException("There is more than one ping status output for task " + task.getId()
						+ " with correlation key " + correlationKey);
			}
		}

		return task;
	}

	public static TaskOutputComponent updatePingStatusOutput(TaskOutputComponent outputComponent, String statusCode)
	{
		if (hasStatusCodeSet(outputComponent))
		{
			updateStatus(outputComponent, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS, statusCode);
		}
		else
		{
			addStatus(outputComponent, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS, statusCode);
		}

		return outputComponent;
	}

	public static Task updatePongStatusOutput(Task task, String statusCode)
	{
		List<Task.TaskOutputComponent> pongStatusOutputs = getOutputsByExtensionUrlAndCodes(task,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS);
		if (pongStatusOutputs.isEmpty())
		{
			task.addOutput(updatePongStatusOutput(new TaskOutputComponent(), statusCode));
		}
		else
		{
			if (pongStatusOutputs.size() == 1)
			{
				updatePongStatusOutput(pongStatusOutputs.get(0), statusCode);
			}
			else
			{
				throw new RuntimeException("There is more than one pong status output for task " + task.getId());
			}
		}

		return task;
	}

	public static TaskOutputComponent updatePongStatusOutput(TaskOutputComponent outputComponent, String statusCode)
	{
		if (hasStatusCodeSet(outputComponent))
		{
			updateStatus(outputComponent, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS, statusCode);
		}
		else
		{
			addStatus(outputComponent, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS, statusCode);
		}

		return outputComponent;
	}

	public static Task updatePingStatusOutput(Task task, Target target)
	{
		List<Task.TaskOutputComponent> outputs = filterByCorrelationKey(
				getOutputsByExtensionUrlAndCodes(task, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS),
				target.getCorrelationKey());
		if (outputs.isEmpty())
		{
			task.addOutput(updateStatusOutput(new TaskOutputComponent(), target));
		}
		else
		{
			if (outputs.size() == 1)
			{
				updateStatusOutput(outputs.get(0), target);
			}
			else
			{
				throw new RuntimeException("There is more than one ping/pong status output for task " + task.getId()
						+ " with correlation key " + target.getCorrelationKey());
			}
		}
		return task;
	}

	public static Task updatePongStatusOutput(Task task, Target target)
	{
		List<Task.TaskOutputComponent> outputs = getOutputsByExtensionUrlAndCodes(task,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS);
		if (outputs.isEmpty())
		{
			task.addOutput(updateStatusOutput(new TaskOutputComponent(), target));
		}
		else
		{
			if (outputs.size() == 1)
			{
				updateStatusOutput(outputs.get(0), target);
			}
			else
			{
				throw new RuntimeException("There is more than one pong status output for task " + task.getId());
			}
		}

		return task;
	}

	public static TaskOutputComponent updateStatusOutput(TaskOutputComponent outputComponent, Target target)
	{
		if (hasTargetSet(outputComponent))
		{
			updateTarget(outputComponent, target);
		}
		else
		{
			addTarget(outputComponent, target);
		}
		return outputComponent;
	}

	public static Task updatePingStatusOutput(Task task, String correlationKey, BigDecimal downloadSpeed,
			BigDecimal uploadSpeed, String statusCode)
	{
		List<Task.TaskOutputComponent> outputs = filterByCorrelationKey(
				getOutputsByExtensionUrlAndCodes(task, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS),
				correlationKey);
		if (outputs.isEmpty())
		{
			task.addOutput(updateStatusOutput(new TaskOutputComponent(), downloadSpeed, uploadSpeed, statusCode));
		}
		else
		{
			if (outputs.size() == 1)
			{
				updateStatusOutput(outputs.get(0), downloadSpeed, uploadSpeed, statusCode);
			}
			else
			{
				throw new RuntimeException("There is more than one ping/pong status output for task " + task.getId()
						+ " with correlation key " + correlationKey);
			}
		}
		return task;
	}

	public static Task updatePongStatusOutput(Task task, BigDecimal downloadSpeed, BigDecimal uploadSpeed,
			String statusCode)
	{
		List<Task.TaskOutputComponent> outputs = getOutputsByExtensionUrlAndCodes(task,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS);
		if (outputs.isEmpty())
		{
			task.addOutput(updateStatusOutput(new TaskOutputComponent(), downloadSpeed, uploadSpeed, statusCode));
		}
		else
		{
			if (outputs.size() == 1)
			{
				updateStatusOutput(outputs.get(0), downloadSpeed, uploadSpeed, statusCode);
			}
			else
			{
				throw new RuntimeException("There is more than one ping/pong status output for task " + task.getId());
			}
		}

		return task;
	}

	public static TaskOutputComponent updateStatusOutput(TaskOutputComponent outputComponent, BigDecimal downloadSpeed,
			BigDecimal uploadSpeed, String networkSpeedUnit)
	{
		if (hasDownloadSpeedSet(outputComponent))
		{
			updateDownloadSpeed(outputComponent, downloadSpeed, networkSpeedUnit);
		}
		else
		{
			addDownloadSpeed(outputComponent, downloadSpeed, networkSpeedUnit);
		}

		if (hasUploadSpeedSet(outputComponent))
		{
			updateUploadSpeed(outputComponent, uploadSpeed, networkSpeedUnit);
		}
		else
		{
			addUploadSpeed(outputComponent, uploadSpeed, networkSpeedUnit);
		}

		return outputComponent;
	}

	public static Task updatePingStatusOutputDownloadSpeed(Task task, String correlationKey, BigDecimal downloadSpeed,
			String networkSpeedUnit)
	{
		List<Task.TaskOutputComponent> outputs = filterByCorrelationKey(
				getOutputsByExtensionUrlAndCodes(task, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS),
				correlationKey);
		if (outputs.isEmpty())
		{
			task.addOutput(updateStatusOutputDownloadSpeed(new TaskOutputComponent(), downloadSpeed, networkSpeedUnit));
		}
		else
		{
			if (outputs.size() == 1)
			{
				updateStatusOutputDownloadSpeed(outputs.get(0), downloadSpeed, networkSpeedUnit);
			}
			else
			{
				throw new RuntimeException("There is more than one ping/pong status output for task " + task.getId()
						+ " with correlation key " + correlationKey);
			}
		}
		return task;
	}

	public static Task updatePongStatusOutputDownloadSpeed(Task task, BigDecimal downloadSpeed, String networkSpeedUnit)
	{
		List<Task.TaskOutputComponent> outputs = getOutputsByExtensionUrlAndCodes(task,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS);
		if (outputs.isEmpty())
		{
			task.addOutput(updateStatusOutputDownloadSpeed(new TaskOutputComponent(), downloadSpeed, networkSpeedUnit));
		}
		else
		{
			if (outputs.size() == 1)
			{
				updateStatusOutputDownloadSpeed(outputs.get(0), downloadSpeed, networkSpeedUnit);
			}
			else
			{
				throw new RuntimeException("There is more than one ping/pong status output for task " + task.getId());
			}
		}

		return task;
	}

	public static TaskOutputComponent updateStatusOutputDownloadSpeed(TaskOutputComponent outputComponent,
			BigDecimal downloadSpeed, String networkSpeedUnit)
	{
		if (hasDownloadSpeedSet(outputComponent))
		{
			updateDownloadSpeed(outputComponent, downloadSpeed, networkSpeedUnit);
		}
		else
		{
			addDownloadSpeed(outputComponent, downloadSpeed, networkSpeedUnit);
		}

		return outputComponent;
	}

	public static Task updatePingStatusOutputUploadSpeed(Task task, String correlationKey, BigDecimal uploadSpeed,
			String networkSpeedUnit)
	{
		List<Task.TaskOutputComponent> outputs = filterByCorrelationKey(
				getOutputsByExtensionUrlAndCodes(task, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS),
				correlationKey);
		if (outputs.isEmpty())
		{
			task.addOutput(updateStatusOutputUploadSpeed(new TaskOutputComponent(), uploadSpeed, networkSpeedUnit));
		}
		else
		{
			if (outputs.size() == 1)
			{
				updateStatusOutputUploadSpeed(outputs.get(0), uploadSpeed, networkSpeedUnit);
			}
			else
			{
				throw new RuntimeException("There is more than one ping/pong status output for task " + task.getId()
						+ " with correlation key " + correlationKey);
			}
		}

		return task;
	}

	public static Task updatePongStatusOutputUploadSpeed(Task task, BigDecimal uploadSpeed, String networkSpeedUnit)
	{
		List<Task.TaskOutputComponent> outputs = getOutputsByExtensionUrlAndCodes(task,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS);
		if (outputs.isEmpty())
		{
			task.addOutput(updateStatusOutputUploadSpeed(new TaskOutputComponent(), uploadSpeed, networkSpeedUnit));
		}
		else
		{
			if (outputs.size() == 1)
			{
				updateStatusOutputUploadSpeed(outputs.get(0), uploadSpeed, networkSpeedUnit);
			}
			else
			{
				throw new RuntimeException("There is more than one ping/pong status output for task " + task.getId());
			}
		}

		return task;
	}

	public static TaskOutputComponent updateStatusOutputUploadSpeed(TaskOutputComponent outputComponent,
			BigDecimal uploadSpeed, String networkSpeedUnit)
	{
		if (hasDownloadSpeedSet(outputComponent))
		{
			updateUploadSpeed(outputComponent, uploadSpeed, networkSpeedUnit);
		}
		else
		{
			addUploadSpeed(outputComponent, uploadSpeed, networkSpeedUnit);
		}

		return outputComponent;
	}

	private static boolean hasTargetSet(TaskOutputComponent outputComponent)
	{
		List<Extension> correlationKeyExtensions = outputComponent
				.getExtensionsByUrl(ConstantsPing.EXTENSION_URL_CORRELATION_KEY);
		List<Extension> organizationIdentifierExtensions = outputComponent
				.getExtensionsByUrl(ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER);
		List<Extension> endpointIdentifierExtensions = outputComponent
				.getExtensionsByUrl(ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER);
		return !correlationKeyExtensions.isEmpty() || !organizationIdentifierExtensions.isEmpty()
				|| !endpointIdentifierExtensions.isEmpty();
	}

	private static boolean hasStatusCodeSet(TaskOutputComponent outputComponent)
	{
		Type valueType = outputComponent.getValue();
		List<Coding> outputTypeCodings = outputComponent.getType().getCoding();

		return (valueType instanceof Coding coding
				&& ConstantsPing.CODESYSTEM_DSF_PING_STATUS.equals(coding.getSystem()))
				|| outputTypeCodings.stream()
						.anyMatch(coding -> ConstantsPing.CODESYSTEM_DSF_PING.equals(coding.getSystem()));
	}

	private static boolean hasNetworkSpeedSet(TaskOutputComponent outputComponent)
	{
		return hasDownloadSpeedSet(outputComponent) && hasUploadSpeedSet(outputComponent);
	}

	private static boolean hasDownloadSpeedSet(TaskOutputComponent outputComponent)
	{
		Extension extension = getOrCreatePingStatusExtension(outputComponent);
		Extension downloadSpeedExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_DOWNLOAD_SPEED);

		return downloadSpeedExtension != null;
	}

	private static boolean hasUploadSpeedSet(TaskOutputComponent outputComponent)
	{
		Extension extension = getOrCreatePingStatusExtension(outputComponent);
		Extension uploadSpeedExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_UPLOAD_SPEED);

		return uploadSpeedExtension != null;
	}

	public static TaskOutputComponent createPingStatusOutput(Target target, String statusCode)
	{
		return createPingStatusOutput(target, statusCode, null);
	}

	public static TaskOutputComponent createPingStatusOutput(Target target, String statusCode, BigDecimal downloadSpeed,
			BigDecimal uploadSpeed, String unit)
	{
		return createPingStatusOutput(target, statusCode, null, downloadSpeed, uploadSpeed, unit);
	}

	public static TaskOutputComponent createPingStatusOutput(Target target, String statusCode,
			List<String> errorMessages)
	{
		return createStatusOutput(target, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS, statusCode,
				errorMessages, null, null, null);
	}

	public static TaskOutputComponent createPingStatusOutput(Target target, String statusCode,
			List<String> errorMessages, BigDecimal downloadSpeed, BigDecimal uploadSpeed, String unit)
	{
		return createStatusOutput(target, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS, statusCode,
				errorMessages, downloadSpeed, uploadSpeed, unit);
	}

	public static TaskOutputComponent createPongStatusOutput(Target target, String statusCode)
	{
		return createPongStatusOutput(target, statusCode, null);
	}

	public static TaskOutputComponent createPongStatusOutput(Target target, String statusCode, BigDecimal downloadSpeed,
			BigDecimal uploadSpeed, String unit)
	{
		return createPongStatusOutput(target, statusCode, null, downloadSpeed, uploadSpeed, unit);
	}

	public static TaskOutputComponent createPongStatusOutput(Target target, String statusCode,
			List<String> errorMessages)
	{
		return createStatusOutput(target, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS, statusCode,
				errorMessages, null, null, null);
	}

	public static TaskOutputComponent createPongStatusOutput(Target target, String statusCode,
			List<String> errorMessages, BigDecimal downloadSpeed, BigDecimal uploadSpeed, String unit)
	{
		return createStatusOutput(target, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS, statusCode,
				errorMessages, downloadSpeed, uploadSpeed, unit);
	}

	private static TaskOutputComponent createStatusOutput(Target target, String outputParameter, String statusCode,
			List<String> errorMessages, BigDecimal downloadSpeed, BigDecimal uploadSpeed, String unit)
	{
		TaskOutputComponent output = new TaskOutputComponent();
		addStatus(output, outputParameter, statusCode);
		addTarget(output, target);
		addErrorMessages(output, errorMessages);
		addNetworkSpeed(output, downloadSpeed, uploadSpeed, unit);

		return output;
	}

	private static TaskOutputComponent addStatus(TaskOutputComponent outputComponent, String outputParameter,
			String statusCode)
	{
		outputComponent.setValue(new Coding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING_STATUS).setCode(statusCode));
		outputComponent.getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING).setCode(outputParameter);
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent updateStatus(TaskOutputComponent outputComponent, String outputParameter,
			String statusCode)
	{
		Type valueType = outputComponent.getValue();
		if (valueType instanceof Coding coding)
		{
			coding.setSystem(ConstantsPing.CODESYSTEM_DSF_PING_STATUS).setCode(statusCode);
		}
		else
		{
			outputComponent
					.setValue(new Coding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING_STATUS).setCode(statusCode));
		}

		List<Coding> outputTypeCodings = outputComponent.getType().getCoding();
		if (outputTypeCodings.isEmpty())
		{
			outputComponent.getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING).setCode(outputParameter);
		}
		else
		{
			if (outputTypeCodings.size() == 1)
			{
				Coding coding = outputTypeCodings.get(0);
				coding.setSystem(ConstantsPing.CODESYSTEM_DSF_PING).setCode(outputParameter);
			}
			else
			{
				outputComponent.getType().setCoding(null);
				outputComponent.getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING)
						.setCode(outputParameter);
			}
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent addTarget(TaskOutputComponent outputComponent, Target target)
	{
		Extension extension = getOrCreatePingStatusExtension(outputComponent);

		extension.addExtension(ConstantsPing.EXTENSION_URL_CORRELATION_KEY, new StringType(target.getCorrelationKey()));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER)
				.setValue(OrganizationIdentifier.withValue(target.getOrganizationIdentifierValue()));
		extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER)
				.setValue(EndpointIdentifier.withValue(target.getEndpointIdentifierValue()));
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent updateTarget(TaskOutputComponent outputComponent, Target target)
	{
		Extension extension = getOrCreatePingStatusExtension(outputComponent);

		Extension correlationKeyExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_CORRELATION_KEY);
		if (correlationKeyExtension != null)
		{
			correlationKeyExtension.setValue(new StringType(target.getCorrelationKey()));
		}
		else
		{
			extension.addExtension(ConstantsPing.EXTENSION_URL_CORRELATION_KEY,
					new StringType(target.getCorrelationKey()));
		}

		Extension organizationIdentifierExtension = extension
				.getExtensionByUrl(ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER);
		if (organizationIdentifierExtension != null)
		{
			organizationIdentifierExtension.setValue(new StringType(target.getOrganizationIdentifierValue()));
		}
		else
		{
			extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER)
					.setValue(OrganizationIdentifier.withValue(target.getOrganizationIdentifierValue()));
		}

		Extension urlEndpointIdentifier = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER);
		if (urlEndpointIdentifier != null)
		{
			urlEndpointIdentifier.setValue(new StringType(target.getEndpointIdentifierValue()));
		}
		else
		{
			extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER)
					.setValue(EndpointIdentifier.withValue(target.getEndpointIdentifierValue()));
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent addErrorMessages(TaskOutputComponent outputComponent, List<String> errorMessages)
	{
		if (errorMessages != null)
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);
			for (String errorMessage : errorMessages)
			{
				extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ERROR_MESSAGE)
						.setValue(new StringType(errorMessage));
			}
		}
		sortStatusOutputExtensions(outputComponent);
		return outputComponent;
	}

	private static TaskOutputComponent updateErrorMessages(TaskOutputComponent outputComponent,
			List<String> errorMessages)
	{
		Extension extension = getOrCreatePingStatusExtension(outputComponent);
		List<Extension> nonErrorExtensions = extension.getExtension().stream()
				.filter(extension1 -> !ConstantsPing.EXTENSION_URL_ERROR_MESSAGE.equals(extension1.getUrl()))
				.collect(Collectors.toCollection(ArrayList::new));

		if (errorMessages != null)
		{
			List<Extension> newErrorExtensions = errorMessages.stream()
					.map(errorMessage -> new Extension(ConstantsPing.EXTENSION_URL_ERROR_MESSAGE,
							new StringType(errorMessage)))
					.collect(Collectors.toCollection(ArrayList::new));
			nonErrorExtensions.addAll(newErrorExtensions);
			extension.setExtension(newErrorExtensions);
		}
		else
		{
			extension.setExtension(nonErrorExtensions);
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent addNetworkSpeed(TaskOutputComponent outputComponent, BigDecimal downloadSpeed,
			BigDecimal uploadSpeed, String unit)
	{
		addDownloadSpeed(outputComponent, downloadSpeed, unit);
		addUploadSpeed(outputComponent, uploadSpeed, unit);

		return outputComponent;
	}

	private static TaskOutputComponent updateNetworkSpeed(TaskOutputComponent outputComponent, BigDecimal downloadSpeed,
			BigDecimal uploadSpeed, String unit)
	{
		updateDownloadSpeed(outputComponent, downloadSpeed, unit);
		updateUploadSpeed(outputComponent, uploadSpeed, unit);
		return outputComponent;
	}

	private static TaskOutputComponent addDownloadSpeed(TaskOutputComponent outputComponent, BigDecimal downloadSpeed,
			String unit)
	{
		if (downloadSpeed != null && unit != null)
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);
			Extension downloadSpeedExtension = extension.addExtension()
					.setUrl(ConstantsPing.EXTENSION_URL_DOWNLOAD_SPEED);
			Extension networkSpeed = downloadSpeedExtension.addExtension()
					.setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED);
			networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_VALUE)
					.setValue(new DecimalType(downloadSpeed));
			networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_UNIT)
					.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_UNITS, unit, null));
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent updateDownloadSpeed(TaskOutputComponent outputComponent,
			BigDecimal downloadSpeed, String unit)
	{
		if (downloadSpeed != null && unit != null)
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);
			Extension downloadSpeedExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_DOWNLOAD_SPEED);
			if (downloadSpeedExtension != null)
			{
				Extension networkSpeedExtension = downloadSpeedExtension
						.getExtensionByUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED);
				if (networkSpeedExtension != null)
				{
					networkSpeedExtension.setExtension(new ArrayList<>());
					List<Extension> extensions = networkSpeedExtension.getExtension();
					extensions.add(new Extension(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_VALUE,
							new DecimalType(downloadSpeed)));
					extensions.add(new Extension(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_UNIT,
							new Coding(ConstantsPing.CODESYSTEM_DSF_PING_UNITS, unit, null)));
				}
			}
			else
			{
				downloadSpeedExtension = extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_DOWNLOAD_SPEED);
				Extension networkSpeed = downloadSpeedExtension.addExtension()
						.setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED);
				networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_VALUE)
						.setValue(new DecimalType(downloadSpeed));
				networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_UNIT)
						.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_UNITS, unit, null));
			}
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent addUploadSpeed(TaskOutputComponent outputComponent, BigDecimal uploadSpeed,
			String unit)
	{
		if (uploadSpeed != null && unit != null)
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);
			Extension uploadSpeedExtension = extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_UPLOAD_SPEED);
			Extension networkSpeed = uploadSpeedExtension.addExtension()
					.setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED);
			networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_VALUE)
					.setValue(new DecimalType(uploadSpeed));
			networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_UNIT)
					.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_UNITS, unit, null));
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent updateUploadSpeed(TaskOutputComponent outputComponent, BigDecimal uploadSpeed,
			String unit)
	{
		if (uploadSpeed != null && unit != null)
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);
			Extension uploadSpeedExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_UPLOAD_SPEED);
			if (uploadSpeedExtension != null)
			{
				Extension networkSpeedExtension = uploadSpeedExtension
						.getExtensionByUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED);
				if (networkSpeedExtension != null)
				{
					networkSpeedExtension.setExtension(new ArrayList<>());
					List<Extension> extensions = networkSpeedExtension.getExtension();
					extensions.add(new Extension(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_VALUE,
							new DecimalType(uploadSpeed)));
					extensions.add(new Extension(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_UNIT,
							new Coding(ConstantsPing.CODESYSTEM_DSF_PING_UNITS, unit, null)));
				}
			}
			else
			{
				uploadSpeedExtension = extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_UPLOAD_SPEED);
				Extension networkSpeed = uploadSpeedExtension.addExtension()
						.setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED);
				networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_VALUE)
						.setValue(new DecimalType(uploadSpeed));
				networkSpeed.addExtension().setUrl(ConstantsPing.EXTENSION_URL_NETWORK_SPEED_UNIT)
						.setValue(new Coding(ConstantsPing.CODESYSTEM_DSF_PING_UNITS, unit, null));
			}
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static Extension getOrCreatePingStatusExtension(TaskOutputComponent outputComponent)
	{
		Optional<Extension> optionalExtension = getPingStatusExtension(outputComponent);
		if (optionalExtension.isPresent())
		{
			return optionalExtension.get();
		}
		else
		{
			Extension extension = outputComponent.addExtension();
			extension.setUrl(appendFhirResourceVersion(ConstantsPing.EXTENSION_URL_PING_STATUS));
			return extension;
		}
	}

	private static Optional<Extension> getPingStatusExtension(Task task)
	{
		Optional<TaskOutputComponent> optPingStatusOutput = task.getOutput().stream()
				.filter(outputComponent -> outputComponent.getExtension().stream()
						.anyMatch(extension -> ConstantsPing.EXTENSION_URL_PING_STATUS.equals(extension.getUrl())))
				.findFirst();
		if (optPingStatusOutput.isPresent())
		{
			return getPingStatusExtension(optPingStatusOutput.get());
		}
		else
		{
			return Optional.empty();
		}
	}

	private static Optional<Extension> getPingStatusExtension(TaskOutputComponent outputComponent)
	{
		List<Extension> pingStatusExtensions = outputComponent.getExtension().stream()
				.filter(extension -> appendFhirResourceVersion(ConstantsPing.EXTENSION_URL_PING_STATUS)
						.equals(extension.getUrl()))
				.toList();
		if (pingStatusExtensions.isEmpty())
		{
			return Optional.empty();
		}
		else
		{
			if (pingStatusExtensions.size() == 1)
			{
				return Optional.of(pingStatusExtensions.get(0));
			}
			else
			{
				throw new RuntimeException(
						"Only one ping status extension is allowed but found " + pingStatusExtensions.size());
			}
		}
	}

	private static List<Task.TaskOutputComponent> getOutputsByExtensionUrlAndCodes(Task task, String... codes)
	{
		return task
				.getOutput().stream().filter(
						outputComponent -> outputComponent.getType().getCoding().stream()
								.anyMatch(coding -> ConstantsPing.CODESYSTEM_DSF_PING.equals(coding.getSystem())
										&& Stream.of(codes).anyMatch(code -> code.equals(coding.getCode())))
								|| outputComponent.getExtension().stream()
										.anyMatch(extension -> appendFhirResourceVersion(
												ConstantsPing.EXTENSION_URL_PING_STATUS).equals(extension.getUrl())))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private static List<Task.TaskOutputComponent> filterByCorrelationKey(List<TaskOutputComponent> outputs,
			String correlationKey)
	{
		return outputs.stream().filter(outputComponent ->
		{
			List<Extension> outputExtensions = outputComponent.getExtension();
			if (outputExtensions.isEmpty())
				return false;
			List<Extension> pingStatusExtensions = outputExtensions.stream()
					.filter(extension -> ConstantsPing.EXTENSION_URL_PING_STATUS.equals(extension.getUrl())).toList();
			if (pingStatusExtensions.isEmpty())
				return false;
			List<Extension> extensionsMatchingCorrelationKey = pingStatusExtensions.stream()
					.filter(extension -> extension.getExtension().stream().anyMatch(
							extension1 -> ConstantsPing.EXTENSION_URL_CORRELATION_KEY.equals(extension1.getUrl())
									&& correlationKey.equals(((StringType) extension1.getValue()).getValue())))
					.toList();
			if (extensionsMatchingCorrelationKey.isEmpty())
				return false;
			if (extensionsMatchingCorrelationKey.size() == 1)
			{
				return true;
			}
			else
			{
				throw new RuntimeException(
						"Only one Task.output.extension.extension with correlationKey is allowed but found "
								+ extensionsMatchingCorrelationKey.size());
			}
		}).collect(Collectors.toCollection(ArrayList::new));
	}

	private static void sortStatusOutputExtensions(Task task)
	{
		List<TaskOutputComponent> outputs = task.getOutput().stream()
				.filter(outputComponent -> ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PING_STATUS
						.equals(outputComponent.getType().getCodingFirstRep().getCode())
						|| ConstantsPing.CODESYSTEM_DSF_PING_VALUE_PONG_STATUS
								.equals(outputComponent.getType().getCodingFirstRep().getCode()))
				.toList();
		outputs.forEach(PingStatusGenerator::sortStatusOutputExtensions);
	}

	private static void sortStatusOutputExtensions(TaskOutputComponent outputComponent)
	{
		Optional<Extension> optPingStatusExtension = getPingStatusExtension(outputComponent);
		if (optPingStatusExtension.isPresent())
		{
			Extension pingStatusExtension = optPingStatusExtension.get();
			List<Extension> extensions = pingStatusExtension.getExtension();
			List<Extension> sortedExtensions = new ArrayList<>();

			// Extensions representing Target
			Optional<Extension> correlationKeyExtension = extensions.stream()
					.filter(extension -> ConstantsPing.EXTENSION_URL_CORRELATION_KEY.equals(extension.getUrl()))
					.findFirst();
			Optional<Extension> organizationIdentifierExtension = extensions.stream()
					.filter(extension -> ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER.equals(extension.getUrl()))
					.findFirst();
			Optional<Extension> endpointIdentifierExtension = extensions.stream()
					.filter(extension -> ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER.equals(extension.getUrl()))
					.findFirst();
			if (correlationKeyExtension.isPresent())
			{
				extensions.remove(correlationKeyExtension.get());
				sortedExtensions.add(correlationKeyExtension.get());
			}
			if (organizationIdentifierExtension.isPresent())
			{
				extensions.remove(organizationIdentifierExtension.get());
				sortedExtensions.add(organizationIdentifierExtension.get());
			}
			if (endpointIdentifierExtension.isPresent())
			{
				extensions.remove(endpointIdentifierExtension.get());
				sortedExtensions.add(endpointIdentifierExtension.get());
			}

			Optional<Extension> downloadSpeedExtension = extensions.stream()
					.filter(extension -> ConstantsPing.EXTENSION_URL_DOWNLOAD_SPEED.equals(extension.getUrl()))
					.findFirst();
			if (downloadSpeedExtension.isPresent())
			{
				extensions.remove(downloadSpeedExtension.get());
				sortedExtensions.add(downloadSpeedExtension.get());
			}

			Optional<Extension> uploadSpeedExtension = extensions.stream()
					.filter(extension -> ConstantsPing.EXTENSION_URL_UPLOAD_SPEED.equals(extension.getUrl()))
					.findFirst();
			if (uploadSpeedExtension.isPresent())
			{
				extensions.remove(uploadSpeedExtension.get());
				sortedExtensions.add(uploadSpeedExtension.get());
			}

			List<Extension> errorMessageExtensions = extensions.stream()
					.filter(extension -> ConstantsPing.EXTENSION_URL_ERROR_MESSAGE.equals(extension.getUrl())).toList();
			if (!errorMessageExtensions.isEmpty())
			{
				extensions.removeAll(errorMessageExtensions);
				sortedExtensions.addAll(errorMessageExtensions);
			}

			sortedExtensions.addAll(extensions);
			pingStatusExtension.setExtension(sortedExtensions);
		}
	}
}
