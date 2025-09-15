package dev.dsf.bpe.util.task.output.generator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.Type;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.PingProcessPluginDefinition;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.v1.constants.NamingSystems.EndpointIdentifier;
import dev.dsf.bpe.v1.constants.NamingSystems.OrganizationIdentifier;
import dev.dsf.bpe.v1.variables.Target;

public final class PingStatusGenerator
{
	private static final String CODESYSTEM_UCUM = "http://unitsofmeasure.org";

	private PingStatusGenerator()
	{
	}

	public static Task updatePongStatusOutput(Task task, List<ProcessError> errors)
	{
		List<Task.TaskOutputComponent> pongStatusOutputs = getOutputsByExtensionUrlAndCodes(task,
				CodeSystem.DsfPing.Code.PONG_STATUS.getValue());
		if (pongStatusOutputs.isEmpty())
		{
			task.addOutput(updateStatusOutput(new TaskOutputComponent(), errors));
		}
		else
		{
			if (pongStatusOutputs.size() == 1)
			{
				updateStatusOutput(pongStatusOutputs.get(0), errors);
			}
			else
			{
				throw new RuntimeException("There is more than one pong status output for task " + task.getId());
			}
		}

		return task;
	}

	public static TaskOutputComponent updateStatusOutput(TaskOutputComponent output, List<ProcessError> errors)
	{
		if (output != null)
		{
			Extension pingStatusExtension = getOrCreatePingStatusExtension(output);
			List<Extension> errorExtensions = pingStatusExtension.getExtension().stream()
					.filter(extension -> ConstantsPing.EXTENSION_URL_ERRORS.equals(extension.getUrl())).toList();
			if (errorExtensions.isEmpty())
			{
				addErrors(output, errors);
			}
			else
			{
				updateErrors(output, errors);
			}
		}

		return output;
	}

	public static Task updatePongStatusOutput(Task task, CodeSystem.DsfPingStatus.Code statusCode)
	{
		List<Task.TaskOutputComponent> pongStatusOutputs = getOutputsByExtensionUrlAndCodes(task,
				CodeSystem.DsfPing.Code.PONG_STATUS.getValue());
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

	private static TaskOutputComponent updatePongStatusOutput(TaskOutputComponent outputComponent,
			CodeSystem.DsfPingStatus.Code statusCode)
	{
		if (hasStatusCodeSet(outputComponent))
		{
			updateStatus(outputComponent, CodeSystem.DsfPing.Code.PONG_STATUS.getValue(), statusCode);
		}
		else
		{
			addStatus(outputComponent, CodeSystem.DsfPing.Code.PONG_STATUS, statusCode);
		}

		return outputComponent;
	}

	public static Task updatePongStatusOutput(Task task, Target target)
	{
		List<Task.TaskOutputComponent> outputs = getOutputsByExtensionUrlAndCodes(task,
				CodeSystem.DsfPing.Code.PONG_STATUS.getValue());
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

	private static TaskOutputComponent updateStatusOutput(TaskOutputComponent outputComponent, Target target)
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

	public static Task updatePongStatusOutputDownloadSpeed(Task task, BigDecimal downloadSpeed,
			CodeSystem.DsfPingUnits.Code networkSpeedUnit)
	{
		List<Task.TaskOutputComponent> outputs = getOutputsByExtensionUrlAndCodes(task,
				CodeSystem.DsfPing.Code.PONG_STATUS.getValue());
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

	private static TaskOutputComponent updateStatusOutputDownloadSpeed(TaskOutputComponent outputComponent,
			BigDecimal downloadSpeed, CodeSystem.DsfPingUnits.Code networkSpeedUnit)
	{
		if (hasDownloadSpeedSet(outputComponent))
		{
			updateDownloadSpeed(outputComponent, downloadSpeed, networkSpeedUnit.name(), CODESYSTEM_UCUM,
					networkSpeedUnit.toUcum());
		}
		else
		{
			addDownloadSpeed(outputComponent, downloadSpeed, networkSpeedUnit.name(), CODESYSTEM_UCUM,
					networkSpeedUnit.toUcum());
		}

		return outputComponent;
	}

	public static Task updatePongStatusOutputUploadSpeed(Task task, BigDecimal uploadSpeed,
			CodeSystem.DsfPingUnits.Code networkSpeedUnit)
	{
		List<Task.TaskOutputComponent> outputs = getOutputsByExtensionUrlAndCodes(task,
				CodeSystem.DsfPing.Code.PONG_STATUS.getValue());
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

	private static TaskOutputComponent updateStatusOutputUploadSpeed(TaskOutputComponent outputComponent,
			BigDecimal uploadSpeed, CodeSystem.DsfPingUnits.Code networkSpeedUnit)
	{
		if (hasDownloadSpeedSet(outputComponent))
		{
			updateUploadSpeed(outputComponent, uploadSpeed, networkSpeedUnit.name(), CODESYSTEM_UCUM,
					networkSpeedUnit.toUcum());
		}
		else
		{
			addUploadSpeed(outputComponent, uploadSpeed, networkSpeedUnit.name(), CODESYSTEM_UCUM,
					networkSpeedUnit.toUcum());
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

		return (valueType instanceof Coding coding && CodeSystem.DsfPingStatus.URL.equals(coding.getSystem()))
				|| outputTypeCodings.stream().anyMatch(coding -> CodeSystem.DsfPing.URL.equals(coding.getSystem()));
	}

	private static boolean hasDownloadSpeedSet(TaskOutputComponent outputComponent)
	{
		Extension extension = getOrCreatePingStatusExtension(outputComponent);
		Extension downloadSpeedExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_DOWNLOAD_SPEED);

		return downloadSpeedExtension != null;
	}

	public static TaskOutputComponent createPingStatusOutput(Target target, CodeSystem.DsfPingStatus.Code statusCode,
			List<ProcessError> errors)
	{
		return createStatusOutput(target, CodeSystem.DsfPing.Code.PING_STATUS, statusCode, errors, null, null, null,
				null, null);
	}

	public static TaskOutputComponent createPingStatusOutput(Target target, CodeSystem.DsfPingStatus.Code statusCode,
			List<ProcessError> errors, BigDecimal downloadSpeed, BigDecimal uploadSpeed,
			CodeSystem.DsfPingUnits.Code unit)
	{
		return createStatusOutput(target, CodeSystem.DsfPing.Code.PING_STATUS, statusCode, errors, downloadSpeed,
				uploadSpeed, unit.name(), CODESYSTEM_UCUM, unit.toUcum());
	}

	public static TaskOutputComponent createPongStatusOutput(Target target, CodeSystem.DsfPingStatus.Code statusCode,
			List<ProcessError> errors)
	{
		return createStatusOutput(target, CodeSystem.DsfPing.Code.PONG_STATUS, statusCode, errors, null, null, null,
				null, null);
	}

	private static TaskOutputComponent createStatusOutput(Target target, CodeSystem.DsfPing.Code outputParameter,
			CodeSystem.DsfPingStatus.Code statusCode, List<ProcessError> errors, BigDecimal downloadSpeed,
			BigDecimal uploadSpeed, String unit, String unitSystem, String unitCode)
	{
		TaskOutputComponent output = new TaskOutputComponent();
		addStatus(output, outputParameter, statusCode);
		addTarget(output, target);
		addErrors(output, errors);
		addNetworkSpeed(output, downloadSpeed, uploadSpeed, unit, unitSystem, unitCode);

		return output;
	}

	private static TaskOutputComponent addStatus(TaskOutputComponent outputComponent,
			CodeSystem.DsfPing.Code outputParameter, CodeSystem.DsfPingStatus.Code statusCode)
	{
		if (outputParameter != null && statusCode != null)
		{
			outputComponent.setValue(CodeSystem.DsfPingStatus.fromCode(statusCode));
			outputComponent.getType().addCoding(CodeSystem.DsfPing.fromCode(outputParameter));
			sortStatusOutputExtensions(outputComponent);
		}

		return outputComponent;
	}

	private static TaskOutputComponent updateStatus(TaskOutputComponent outputComponent, String outputParameter,
			CodeSystem.DsfPingStatus.Code statusCode)
	{
		Type valueType = outputComponent.getValue();
		if (valueType instanceof Coding coding)
		{
			coding.setSystem(CodeSystem.DsfPingStatus.URL).setCode(statusCode.getValue())
					.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);
		}
		else
		{
			outputComponent.setValue(CodeSystem.DsfPingStatus.fromCode(statusCode));
		}

		List<Coding> outputTypeCodings = outputComponent.getType().getCoding();
		if (outputTypeCodings.isEmpty())
		{
			outputComponent.getType().addCoding().setSystem(CodeSystem.DsfPing.URL).setCode(outputParameter);
		}
		else
		{
			if (outputTypeCodings.size() == 1)
			{
				Coding coding = outputTypeCodings.get(0);
				coding.setSystem(CodeSystem.DsfPing.URL).setCode(outputParameter);
			}
			else
			{
				outputComponent.getType().setCoding(null);
				outputComponent.getType().addCoding().setSystem(CodeSystem.DsfPing.URL).setCode(outputParameter);
			}
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent addTarget(TaskOutputComponent outputComponent, Target target)
	{
		if (target != null)
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);

			extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ORGANIZATION_IDENTIFIER)
					.setValue(OrganizationIdentifier.withValue(target.getOrganizationIdentifierValue()));
			extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_ENDPOINT_IDENTIFIER)
					.setValue(EndpointIdentifier.withValue(target.getEndpointIdentifierValue()));
			extension.addExtension(ConstantsPing.EXTENSION_URL_CORRELATION_KEY,
					new StringType(target.getCorrelationKey()));
			sortStatusOutputExtensions(outputComponent);
		}

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

	private static TaskOutputComponent addErrors(TaskOutputComponent outputComponent, List<ProcessError> errors)
	{
		if (errors != null && !errors.isEmpty())
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);
			Extension errorsExtension = getOrCreateErrorsExtension(extension);
			for (ProcessError error : errors)
			{
				errorsExtension.addExtension(ProcessError.toExtension(error));
			}
		}
		sortStatusOutputExtensions(outputComponent);
		return outputComponent;
	}

	private static TaskOutputComponent updateErrors(TaskOutputComponent outputComponent, List<ProcessError> errors)
	{
		Extension extension = getOrCreatePingStatusExtension(outputComponent);
		Extension errorsExtension = getOrCreateErrorsExtension(extension);

		if (errors != null)
		{
			List<Extension> newErrorExtensions = errors.stream().map(ProcessError::toExtension)
					.collect(Collectors.toCollection(ArrayList::new));
			errorsExtension.setExtension(newErrorExtensions);
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent addNetworkSpeed(TaskOutputComponent outputComponent, BigDecimal downloadSpeed,
			BigDecimal uploadSpeed, String unit, String unitSystem, String unitCode)
	{
		addDownloadSpeed(outputComponent, downloadSpeed, unit, unitSystem, unitCode);
		addUploadSpeed(outputComponent, uploadSpeed, unit, unitSystem, unitCode);

		return outputComponent;
	}

	private static TaskOutputComponent addDownloadSpeed(TaskOutputComponent outputComponent, BigDecimal downloadSpeed,
			String unit, String unitSystem, String unitCode)
	{
		if (downloadSpeed != null && unit != null && unitSystem != null && unitCode != null)
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);
			Extension downloadSpeedExtension = extension.addExtension()
					.setUrl(ConstantsPing.EXTENSION_URL_DOWNLOAD_SPEED);
			Quantity quantity = new Quantity();
			quantity.setValue(downloadSpeed);
			quantity.setUnit(unit);
			quantity.setSystem(unitSystem);
			quantity.setCode(unitCode);
			downloadSpeedExtension.setValue(quantity);
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent updateDownloadSpeed(TaskOutputComponent outputComponent,
			BigDecimal downloadSpeed, String unit, String unitSystem, String unitCode)
	{
		if (downloadSpeed != null && unit != null && unitSystem != null && unitCode != null)
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);
			Extension downloadSpeedExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_DOWNLOAD_SPEED);
			if (downloadSpeedExtension != null)
			{
				Quantity quantity = new Quantity();
				quantity.setValue(downloadSpeed);
				quantity.setUnit(unit);
				quantity.setSystem(unitSystem);
				quantity.setCode(unitCode);
				downloadSpeedExtension.setValue(quantity);
			}
			else
			{
				downloadSpeedExtension = extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_DOWNLOAD_SPEED);
				Quantity quantity = new Quantity();
				quantity.setValue(downloadSpeed);
				quantity.setUnit(unit);
				quantity.setSystem(unitSystem);
				quantity.setCode(unitCode);
				downloadSpeedExtension.setValue(quantity);
			}
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent addUploadSpeed(TaskOutputComponent outputComponent, BigDecimal uploadSpeed,
			String unit, String unitSystem, String unitCode)
	{
		if (uploadSpeed != null && unit != null && unitSystem != null && unitCode != null)
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);
			Extension uploadSpeedExtension = extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_UPLOAD_SPEED);
			Quantity quantity = new Quantity();
			quantity.setValue(uploadSpeed);
			quantity.setUnit(unit);
			quantity.setSystem(unitSystem);
			quantity.setCode(unitCode);
			uploadSpeedExtension.setValue(quantity);
		}
		sortStatusOutputExtensions(outputComponent);

		return outputComponent;
	}

	private static TaskOutputComponent updateUploadSpeed(TaskOutputComponent outputComponent, BigDecimal uploadSpeed,
			String unit, String unitSystem, String unitCode)
	{
		if (uploadSpeed != null && unit != null && unitSystem != null && unitCode != null)
		{
			Extension extension = getOrCreatePingStatusExtension(outputComponent);
			Extension uploadSpeedExtension = extension.getExtensionByUrl(ConstantsPing.EXTENSION_URL_UPLOAD_SPEED);
			if (uploadSpeedExtension != null)
			{
				Quantity quantity = (Quantity) uploadSpeedExtension.getValue();
				if (quantity != null)
				{
					quantity.setValue(uploadSpeed);
					quantity.setUnit(unit);
					quantity.setSystem(unitSystem);
					quantity.setCode(unitCode);
				}
			}
			else
			{
				uploadSpeedExtension = extension.addExtension().setUrl(ConstantsPing.EXTENSION_URL_UPLOAD_SPEED);
				Quantity quantity = new Quantity();
				quantity.setValue(uploadSpeed);
				quantity.setUnit(unit);
				quantity.setSystem(unitSystem);
				quantity.setCode(unitCode);
				uploadSpeedExtension.setValue(quantity);
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
			extension.setUrl(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_PING_STATUS);
			return extension;
		}
	}

	private static Optional<Extension> getPingStatusExtension(TaskOutputComponent outputComponent)
	{
		List<Extension> pingStatusExtensions = outputComponent.getExtension().stream().filter(
				extension -> ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_PING_STATUS.equals(extension.getUrl()))
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

	private static Extension getOrCreateErrorsExtension(Extension extension)
	{
		Optional<Extension> optionalExtension = getErrorsExtension(extension);
		if (optionalExtension.isPresent())
		{
			return optionalExtension.get();
		}
		else
		{
			Extension errorsExtension = extension.addExtension();
			errorsExtension.setUrl(ConstantsPing.EXTENSION_URL_ERRORS);
			return errorsExtension;
		}
	}

	private static Optional<Extension> getErrorsExtension(Extension extension)
	{
		List<Extension> errorsExtensions = extension.getExtension().stream()
				.filter(ex -> ConstantsPing.EXTENSION_URL_ERRORS.equals(ex.getUrl())).toList();
		if (errorsExtensions.isEmpty())
		{
			return Optional.empty();
		}
		else
		{
			if (errorsExtensions.size() == 1)
			{
				return Optional.of(errorsExtensions.get(0));
			}
			else
			{
				throw new RuntimeException("Only one errors extension is allowed but found " + errorsExtensions.size());
			}
		}
	}

	private static List<Task.TaskOutputComponent> getOutputsByExtensionUrlAndCodes(Task task, String... codes)
	{
		return task.getOutput().stream()
				.filter(outputComponent -> outputComponent.getType().getCoding().stream()
						.anyMatch(coding -> CodeSystem.DsfPing.URL.equals(coding.getSystem())
								&& Stream.of(codes).anyMatch(code -> code.equals(coding.getCode())))
						|| outputComponent.getExtension().stream()
								.anyMatch(extension -> ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_PING_STATUS
										.equals(extension.getUrl())))
				.collect(Collectors.toCollection(ArrayList::new));
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
			if (correlationKeyExtension.isPresent())
			{
				extensions.remove(correlationKeyExtension.get());
				sortedExtensions.add(correlationKeyExtension.get());
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

			Optional<Extension> errorsExtension = extensions.stream()
					.filter(extension -> ConstantsPing.EXTENSION_URL_ERRORS.equals(extension.getUrl())).findFirst();
			if (errorsExtension.isPresent())
			{
				extensions.remove(errorsExtension.get());
				sortedExtensions.add(errorsExtension.get());
			}

			sortedExtensions.addAll(extensions);
			pingStatusExtension.setExtension(sortedExtensions);
		}
	}
}
