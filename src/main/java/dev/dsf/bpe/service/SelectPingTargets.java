package dev.dsf.bpe.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Endpoint.EndpointStatus;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.constants.NamingSystems.EndpointIdentifier;
import dev.dsf.bpe.v1.constants.NamingSystems.OrganizationIdentifier;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class SelectPingTargets extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectPingTargets.class);

	private static final Pattern endpointResouceTypes = Pattern.compile(
			"Endpoint|HealthcareService|ImagingStudy|InsurancePlan|Location|Organization|OrganizationAffiliation|PractitionerRole");

	public SelectPingTargets(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Stream<Endpoint> targetEndpoints = getTargetEndpointsSearchParameter(variables).map(this::searchForEndpoints)
				.orElse(allEndpoints()).filter(isLocalEndpoint().negate());

		List<Organization> remoteOrganizations = api.getOrganizationProvider().getRemoteOrganizations();
		Map<String, Identifier> organizationIdentifierByOrganizationId = remoteOrganizations.stream().collect(
				Collectors.toMap(o -> o.getIdElement().getIdPart(), o -> OrganizationIdentifier.findFirst(o).get()));

		Stream<Endpoint> remoteTargetEndpointsWithActiveOrganization = targetEndpoints
				.filter(e -> getOrganizationIdentifier(e, organizationIdentifierByOrganizationId).isPresent());

		List<Target> targets = remoteTargetEndpointsWithActiveOrganization.map(e ->
		{
			String organizationIdentifier = getOrganizationIdentifier(e, organizationIdentifierByOrganizationId).get();
			String endpointIdentifier = EndpointIdentifier.findFirst(e).map(Identifier::getValue).get();
			String endpointAddress = e.getAddress();

			return variables.createTarget(organizationIdentifier, endpointIdentifier, endpointAddress,
					UUID.randomUUID().toString());
		}).collect(Collectors.toList());

		variables.setTargets(variables.createTargets(targets));
	}

	private Optional<UriComponents> getTargetEndpointsSearchParameter(Variables variables)
	{
		Task mainTask = variables.getStartTask();
		return api.getTaskHelper()
				.getFirstInputParameterStringValue(mainTask, ConstantsPing.CODESYSTEM_DSF_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_VALUE_TARGET_ENDPOINTS)
				.map(requestUrl -> UriComponentsBuilder.fromUriString(requestUrl).build());
	}

	private Stream<Endpoint> searchForEndpoints(UriComponents searchParameters)
	{
		return searchForEndpoints(searchParameters, 1, 0);
	}

	private Stream<Endpoint> searchForEndpoints(UriComponents searchParameters, int page, int currentTotal)
	{
		if (searchParameters.getPathSegments().isEmpty())
			return Stream.empty();

		Optional<Class<? extends Resource>> resourceType = getResourceType(searchParameters);
		if (resourceType.isEmpty())
			return Stream.empty();

		Map<String, List<String>> queryParameters = new HashMap<String, List<String>>();
		queryParameters.putAll(searchParameters.getQueryParams());
		queryParameters.put("_page", Collections.singletonList(String.valueOf(page)));

		Bundle searchResult = api.getFhirWebserviceClientProvider().getLocalWebserviceClient()
				.searchWithStrictHandling(resourceType.get(), queryParameters);

		if (searchResult.getTotal() > currentTotal + searchResult.getEntry().size())
			return Stream.concat(toEndpoints(searchResult),
					searchForEndpoints(searchParameters, page + 1, currentTotal + searchResult.getEntry().size()));
		else
			return toEndpoints(searchResult);
	}

	@SuppressWarnings("unchecked")
	private Optional<Class<? extends Resource>> getResourceType(UriComponents searchParameters)
	{
		if (searchParameters.getPathSegments().isEmpty())
			return Optional.empty();

		String type = searchParameters.getPathSegments().get(searchParameters.getPathSegments().size() - 1);
		if (!endpointResouceTypes.matcher(type).matches())
			return Optional.empty();

		try
		{
			return Optional.of((Class<? extends Resource>) Class.forName("org.hl7.fhir.r4.model." + type));
		}
		catch (ClassNotFoundException e)
		{
			logger.error("Unable to find class for FHIR resource type " + type, e);
			return Optional.empty();
		}
	}

	private Stream<Endpoint> allEndpoints()
	{
		return allEndpoints(1, 0);
	}

	private Predicate<? super Endpoint> isLocalEndpoint()
	{
		return e -> Objects.equals(api.getEndpointProvider().getLocalEndpointAddress(), e.getAddress());
	}

	private Stream<Endpoint> allEndpoints(int page, int currentTotal)
	{
		Bundle searchResult = api.getFhirWebserviceClientProvider().getLocalWebserviceClient().searchWithStrictHandling(
				Endpoint.class,
				Map.of("status", Collections.singletonList("active"), "identifier",
						Collections.singletonList("http://dsf.dev/sid/endpoint-identifier|"), "_page",
						Collections.singletonList(String.valueOf(page))));

		if (searchResult.getTotal() > currentTotal + searchResult.getEntry().size())
			return Stream.concat(toEndpoints(searchResult),
					allEndpoints(page + 1, currentTotal + searchResult.getEntry().size()));
		else
			return toEndpoints(searchResult);
	}

	private Stream<Endpoint> toEndpoints(Bundle searchResult)
	{
		Objects.requireNonNull(searchResult, "searchResult");

		return searchResult.getEntry().stream().filter(BundleEntryComponent::hasResource)
				.filter(e -> e.getResource() instanceof Endpoint).map(e -> (Endpoint) e.getResource())
				.filter(Endpoint::hasStatus).filter(e -> EndpointStatus.ACTIVE.equals(e.getStatus()));
	}

	private Optional<String> getOrganizationIdentifier(Endpoint endpoint,
			Map<String, Identifier> organizationIdentifierByOrganizationId)
	{
		if (!endpoint.hasManagingOrganization() || !endpoint.getManagingOrganization().hasReferenceElement())
			return Optional.empty();

		return Optional
				.ofNullable(organizationIdentifierByOrganizationId
						.get(endpoint.getManagingOrganization().getReferenceElement().getIdPart()))
				.map(Identifier::getValue);
	}
}