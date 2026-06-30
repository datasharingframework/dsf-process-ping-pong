package dev.dsf.bpe.service.ping;

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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.constants.NamingSystems.EndpointIdentifier;
import dev.dsf.bpe.v2.constants.NamingSystems.OrganizationIdentifier;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.error.ServiceTaskErrorHandler;
import dev.dsf.bpe.v2.error.impl.DefaultServiceTaskErrorHandler;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class SelectPingTargets implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SelectPingTargets.class);
	private static final Pattern endpointResouceTypes = Pattern.compile(
			"Endpoint|HealthcareService|ImagingStudy|InsurancePlan|Location|Organization|OrganizationAffiliation|PractitionerRole");

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Stream<Endpoint> targetEndpoints = getTargetEndpointsSearchParameter(api, variables)
				.map(uriComponents -> searchForEndpoints(api, uriComponents)).orElse(allEndpoints(api))
				.filter(isLocalEndpoint(api).negate());

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

	@Override
	public ServiceTaskErrorHandler getErrorHandler()
	{
		return new DefaultServiceTaskErrorHandler()
		{
			@Override
			public Exception handleException(ProcessPluginApi api, Variables variables, Exception exception)
			{
				logger.error("Unexpected error while selecting ping targets.", exception);
				ErrorListUtils.add(new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null), variables);
				return new ErrorBoundaryEvent(ConstantsPing.BPMN_ERROR_CODE_UNEXPECTED_ERROR,
						ConstantsPing.BPMN_ERROR_MESSAGE_UNEXPECTED_ERROR);
			}
		};
	}

	private Optional<UriComponents> getTargetEndpointsSearchParameter(ProcessPluginApi api, Variables variables)
	{
		Task mainTask = variables.getStartTask();
		return api.getTaskHelper()
				.getFirstInputParameterStringValue(mainTask, CodeSystem.DsfPing.URL,
						CodeSystem.DsfPing.Code.TARGET_ENDPOINTS.getValue())
				.map(requestUrl -> UriComponentsBuilder.fromUriString(requestUrl).build());
	}

	private Stream<Endpoint> searchForEndpoints(ProcessPluginApi api, UriComponents searchParameters)
	{
		return searchForEndpoints(api, searchParameters, 1, 0);
	}

	private Stream<Endpoint> searchForEndpoints(ProcessPluginApi api, UriComponents searchParameters, int page,
			int currentTotal)
	{
		if (searchParameters.getPathSegments().isEmpty())
			return Stream.empty();

		Optional<Class<? extends Resource>> resourceType = getResourceType(searchParameters);
		if (resourceType.isEmpty())
			return Stream.empty();

		Map<String, List<String>> queryParameters = new HashMap<>();
		queryParameters.putAll(searchParameters.getQueryParams());
		queryParameters.put("_page", Collections.singletonList(String.valueOf(page)));

		Bundle searchResult = api.getDsfClientProvider().getLocal().searchWithStrictHandling(resourceType.get(),
				queryParameters);

		if (searchResult.getTotal() > currentTotal + searchResult.getEntry().size())
			return Stream.concat(toEndpoints(searchResult),
					searchForEndpoints(api, searchParameters, page + 1, currentTotal + searchResult.getEntry().size()));
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

	private Stream<Endpoint> allEndpoints(ProcessPluginApi api)
	{
		return allEndpoints(api, 1, 0);
	}

	private Predicate<? super Endpoint> isLocalEndpoint(ProcessPluginApi api)
	{
		return e -> Objects.equals(api.getEndpointProvider().getLocalEndpointAddress(), e.getAddress());
	}

	private Stream<Endpoint> allEndpoints(ProcessPluginApi api, int page, int currentTotal)
	{
		Bundle searchResult = api.getDsfClientProvider().getLocal().searchWithStrictHandling(Endpoint.class,
				Map.of("status", Collections.singletonList("active"), "identifier",
						Collections.singletonList("http://dsf.dev/sid/endpoint-identifier|"), "_page",
						Collections.singletonList(String.valueOf(page))));

		if (searchResult.getTotal() > currentTotal + searchResult.getEntry().size())
			return Stream.concat(toEndpoints(searchResult),
					allEndpoints(api, page + 1, currentTotal + searchResult.getEntry().size()));
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