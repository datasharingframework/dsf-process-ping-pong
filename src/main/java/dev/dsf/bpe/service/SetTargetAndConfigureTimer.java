package dev.dsf.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsBase;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.delegate.AbstractServiceDelegate;
import dev.dsf.fhir.authorization.read.ReadAccessHelper;
import dev.dsf.fhir.client.FhirWebserviceClientProvider;
import dev.dsf.fhir.organization.EndpointProvider;
import dev.dsf.fhir.organization.OrganizationProvider;
import dev.dsf.fhir.task.TaskHelper;
import dev.dsf.fhir.variables.Target;
import dev.dsf.fhir.variables.TargetValues;

public class SetTargetAndConfigureTimer extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(SetTargetAndConfigureTimer.class);

	private final OrganizationProvider organizationProvider;
	private final EndpointProvider endpointProvider;

	public SetTargetAndConfigureTimer(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider,
			EndpointProvider endpointProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.organizationProvider = organizationProvider;
		this.endpointProvider = endpointProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
		Objects.requireNonNull(endpointProvider, "endpointProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws BpmnError, Exception
	{
		String timerInterval = getTimerInterval(execution);
		logger.debug("Setting variable '{}' to {}", ConstantsPing.BPMN_EXECUTION_VARIABLE_TIMER_INTERVAL,
				timerInterval);
		execution.setVariable(ConstantsPing.BPMN_EXECUTION_VARIABLE_TIMER_INTERVAL,
				Variables.stringValue(timerInterval));

		execution.setVariable(ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET,
				TargetValues.create(Target.createUniDirectionalTarget(organizationProvider.getLocalIdentifierValue(),
						endpointProvider.getLocalEndpointIdentifier().getValue(),
						getFhirWebserviceClientProvider().getLocalBaseUrl())));
	}

	private String getTimerInterval(DelegateExecution execution)
	{
		return getTaskHelper()
				.getFirstInputParameterStringValue(getLeadingTaskFromExecutionVariables(execution),
						ConstantsPing.CODESYSTEM_DSF_PING, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_TIMER_INTERVAL)
				.orElse(ConstantsPing.TIMER_INTERVAL_DEFAULT_VALUE);
	}
}
