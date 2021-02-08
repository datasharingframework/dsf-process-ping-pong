package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TTP_IDENTIFIER;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.TargetValues;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class SelectResponseTargetTtp extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public SelectResponseTargetTtp(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper);
		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		String ttpIdentifier = (String) execution.getVariable(BPMN_EXECUTION_VARIABLE_TTP_IDENTIFIER);
		String correlationKey = getCorrelationKey(execution);

		Target ttpTarget = Target.createBiDirectionalTarget(ttpIdentifier, correlationKey);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGET, TargetValues.create(ttpTarget));
	}

	private String getCorrelationKey(DelegateExecution execution)
	{
		Task task = getCurrentTaskFromExecutionVariables();

		return getTaskHelper().getFirstInputParameterStringValue(task, ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
				ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY).orElseThrow(
				() -> new IllegalStateException(
						"No correlation key found, this error should have been caught by resource validation"));
	}
}
