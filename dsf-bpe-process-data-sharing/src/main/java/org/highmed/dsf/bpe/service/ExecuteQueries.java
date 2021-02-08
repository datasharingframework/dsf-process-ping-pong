package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_QUERIES;

import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.openehr.client.OpenEhrClient;
import org.highmed.openehr.model.structure.ResultSet;

public class ExecuteQueries extends AbstractServiceDelegate
{
	private final OpenEhrClient openehrClient;
	private final OrganizationProvider organizationProvider;

	public ExecuteQueries(FhirWebserviceClientProvider clientProvider, OpenEhrClient openehrClient,
			TaskHelper taskHelper, OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);

		this.openehrClient = openehrClient;
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(openehrClient, "openehrClient");
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		// <groupId, query>
		@SuppressWarnings("unchecked")
		Map<String, String> queries = (Map<String, String>) execution.getVariable(BPMN_EXECUTION_VARIABLE_QUERIES);

		queries.forEach(this::executeQuery);
	}

	private void executeQuery(String cohortId, String cohortQuery)
	{
		// TODO We might want to introduce a more complex result type to represent a count,
		//      errors and possible meta-data.

		ResultSet resultSet = openehrClient.query(cohortQuery, null);
	}
}