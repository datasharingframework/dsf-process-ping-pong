package dev.dsf.bpe.listener;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.PingProcessPluginDefinition;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.ProcessPluginDeploymentStateListener;
import dev.dsf.fhir.client.FhirWebserviceClient;

public class PingPongProcessPluginDeploymentStateListener
		implements ProcessPluginDeploymentStateListener, InitializingBean
{
	private final ProcessPluginApi api;

	public PingPongProcessPluginDeploymentStateListener(ProcessPluginApi api)
	{
		this.api = api;
	}

	@Override
	public void afterPropertiesSet()
	{
		Objects.requireNonNull(api, "api");
	}

	@Override
	public void onProcessesDeployed(List<String> activeProcesses)
	{
		updateOlderResourcesIfCurrentIsNewestResource(dev.dsf.bpe.CodeSystem.DsfPing.URL, CodeSystem.class,
				adaptCodeSystems());

		updateOlderResourcesIfCurrentIsNewestResource(dev.dsf.bpe.CodeSystem.DsfPingStatus.URL, CodeSystem.class,
				adaptCodeSystems());

		updateOlderResourcesIfCurrentIsNewestResource(dev.dsf.bpe.CodeSystem.DsfPingUnits.URL, CodeSystem.class,
				adaptCodeSystems());

		updateOlderResourcesIfCurrentIsNewestResource(dev.dsf.bpe.CodeSystem.DsfPingError.URL, CodeSystem.class,
				adaptCodeSystems());

		updateOlderResourcesIfCurrentIsNewestResource(ConstantsPing.STRUCTURE_DEFINITION_URL_EXTENSION_PING_STATUS,
				StructureDefinition.class, adaptExtensionStructureDefinitions());

		updateDraftTaskResources();
	}

	private void updateDraftTaskResources()
	{
		FhirWebserviceClient client = api.getFhirWebserviceClientProvider().getLocalWebserviceClient();

		List<String> draftTaskResourceProfiles = List.of("http://dsf.dev/fhir/StructureDefinition/task-start-ping",
				"http://dsf.dev/fhir/StructureDefinition/task-start-ping-autostart");

		for (String profile : draftTaskResourceProfiles)
		{
			Optional<Task> optionalTask = searchTask(profile, PingProcessPluginDefinition.RESOURCE_VERSION).getEntry()
					.stream().map(Bundle.BundleEntryComponent::getResource).map(Task.class::cast).findFirst();

			if (optionalTask.isPresent())
			{
				Task toUpdate = optionalTask.get();
				adaptDraftTask(toUpdate);
				client.update(toUpdate);
			}
		}
	}

	private void adaptDraftTask(Task task)
	{
		Coding downloadResourceSizeBytesCoding = new Coding();
		downloadResourceSizeBytesCoding.setSystem(dev.dsf.bpe.CodeSystem.DsfPing.URL)
				.setCode(dev.dsf.bpe.CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue())
				.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);

		Optional<Task.ParameterComponent> optInput = api.getTaskHelper().getFirstInputParameter(task,
				downloadResourceSizeBytesCoding, DecimalType.class);
		if (optInput.isEmpty())
		{
			Task.ParameterComponent downloadResourceSizeBytes = new Task.ParameterComponent();
			downloadResourceSizeBytes.getType().addCoding(downloadResourceSizeBytesCoding);
			downloadResourceSizeBytes.setValue(new DecimalType(ConstantsPing.DOWNLOAD_RESOURCE_SIZE_BYTES_DEFAULT));
			task.addInput(downloadResourceSizeBytes);
		}

		Coding pongTimeoutDurationCoding = new Coding();
		pongTimeoutDurationCoding.setSystem(dev.dsf.bpe.CodeSystem.DsfPing.URL)
				.setCode(dev.dsf.bpe.CodeSystem.DsfPing.Code.PONG_TIMEOUT_DURATION_ISO_8601.getValue())
				.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);

		optInput = api.getTaskHelper().getFirstInputParameter(task, pongTimeoutDurationCoding, StringType.class);
		if (optInput.isEmpty())
		{
			Task.ParameterComponent pongTimeoutDuration = new Task.ParameterComponent();
			pongTimeoutDuration.getType().addCoding(pongTimeoutDurationCoding);
			pongTimeoutDuration.setValue(new StringType(ConstantsPing.PONG_TIMEOUT_DURATION_DEFAULT_VALUE));
			task.addInput(pongTimeoutDuration);
		}
	}

	private <T extends MetadataResource> void updateOlderResourcesIfCurrentIsNewestResource(String url, Class<T> type,
			BiConsumer<T, List<T>> converter)
	{
		Bundle searchResult = search(type, url);
		List<T> resources = extractAndSortResources(searchResult, type, url);

		if (currentIsNewestResource(resources))
		{
			T currentResource = resources.get(resources.size() - 1);
			List<T> oldResources = resources.subList(0, resources.size() - 1);
			converter.accept(currentResource, oldResources);
		}
	}

	private Bundle search(Class<? extends Resource> type, String url)
	{
		return api.getFhirWebserviceClientProvider().getLocalWebserviceClient().search(type,
				Map.of("url", List.of(url)));
	}

	private Bundle searchTask(String profile, String version)
	{
		return api.getFhirWebserviceClientProvider().getLocalWebserviceClient().search(Task.class,
				Map.of("_profile", List.of(profile + "|" + version), "status", List.of("draft")));
	}

	private <T extends MetadataResource> List<T> extractAndSortResources(Bundle bundle, Class<T> type, String url)
	{
		return bundle.getEntry().stream().filter(Bundle.BundleEntryComponent::hasResource)
				.map(Bundle.BundleEntryComponent::getResource).filter(type::isInstance).map(type::cast)
				.filter(m -> url.equals(m.getUrl())).sorted((r1, r2) ->
				{
					MinorMajorVersion version1 = getMajorMinorVersion(r1.getVersion().substring(0, 3));
					MinorMajorVersion version2 = getMajorMinorVersion(r2.getVersion().substring(0, 3));

					if (version1.major > version2.major)
					{
						return 1;
					}
					else if (version1.major < version2.major)
					{
						return -1;
					}
					else
					{
						return Integer.compare(version1.minor, version2.minor);
					}
				}).toList();
	}

	private boolean currentIsNewestResource(List<? extends MetadataResource> resources)
	{
		return !resources.isEmpty() && PingProcessPluginDefinition.RESOURCE_VERSION
				.equals(resources.get(resources.size() - 1).getVersion());
	}

	private <T> Optional<T> getNewestResource(List<T> resources)
	{
		return resources.isEmpty() ? Optional.empty() : Optional.of(resources.get(resources.size() - 1));
	}

	private MinorMajorVersion getMajorMinorVersion(String version)
	{
		if (version.matches("\\d\\.\\d"))
		{
			String[] minorMajor = version.split("\\.");
			return new MinorMajorVersion(Integer.parseInt(minorMajor[0]), Integer.parseInt(minorMajor[1]));
		}

		throw new RuntimeException("Fhir resource version " + version + " does not match regex \\d\\.\\d");
	}

	private BiConsumer<CodeSystem, List<CodeSystem>> adaptCodeSystems()
	{
		return (currentResource, olderResources) ->
		{
			List<CodeSystem> codeSystemsWithNonMatchingConceptCodes = filterCodeSystemsWithNonMatchingConceptCodesAndAdaptToCurrentCodeSystemConceptCodes(
					currentResource, olderResources);
			updateResources(codeSystemsWithNonMatchingConceptCodes);
		};
	}

	private List<CodeSystem> filterCodeSystemsWithNonMatchingConceptCodesAndAdaptToCurrentCodeSystemConceptCodes(
			CodeSystem currentCodeSystem, List<CodeSystem> olderCodeSystems)
	{
		Set<String> currentConceptCodes = getConceptCodes(currentCodeSystem);
		return olderCodeSystems.stream().filter(c -> !currentConceptCodes.equals(getConceptCodes(c)))
				.map(c -> c.setConcept(currentCodeSystem.getConcept())).toList();
	}

	private Set<String> getConceptCodes(CodeSystem codeSystem)
	{
		return codeSystem.getConcept().stream().map(CodeSystem.ConceptDefinitionComponent::getCode)
				.collect(Collectors.toSet());
	}

	private BiConsumer<StructureDefinition, List<StructureDefinition>> adaptExtensionStructureDefinitions()
	{
		return (currentResource, olderResources) ->
		{
			overrideDifferentialForOlderExtensionStructureDefinitions(olderResources,
					currentResource.getDifferential());
			updateResources(olderResources);
		};
	}

	private void overrideDifferentialForOlderExtensionStructureDefinitions(
			List<StructureDefinition> olderStructureDefinitions,
			StructureDefinition.StructureDefinitionDifferentialComponent newDifferential)
	{
		olderStructureDefinitions.forEach(olderStructureDefinition ->
		{
			olderStructureDefinition.setDifferential(newDifferential);
		});
	}

	private void updateResources(List<? extends MetadataResource> resources)
	{
		resources.forEach(m -> api.getFhirWebserviceClientProvider().getLocalWebserviceClient().update(m));
	}

	private record MinorMajorVersion(int major, int minor)
	{
	}
}
