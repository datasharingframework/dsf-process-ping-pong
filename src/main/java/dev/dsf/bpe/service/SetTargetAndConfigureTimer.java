package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class SetTargetAndConfigureTimer extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(SetTargetAndConfigureTimer.class);

	public SetTargetAndConfigureTimer(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		String timerInterval = getTimerInterval(variables);
		logger.debug("Setting variable '{}' to {}", ConstantsPing.BPMN_EXECUTION_VARIABLE_TIMER_INTERVAL,
				timerInterval);

		variables.setString(ConstantsPing.BPMN_EXECUTION_VARIABLE_TIMER_INTERVAL, timerInterval);
		variables.setTarget(
				variables.createTarget(api.getOrganizationProvider().getLocalOrganizationIdentifierValue().get(),
						api.getEndpointProvider().getLocalEndpointIdentifierValue().get(),
						api.getEndpointProvider().getLocalEndpointAddress()));
	}

	private String getTimerInterval(Variables variables)
	{
		return api.getTaskHelper()
				.getFirstInputParameterStringValue(variables.getStartTask(), ConstantsPing.CODESYSTEM_DSF_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_VALUE_TIMER_INTERVAL)
				.orElse(ConstantsPing.TIMER_INTERVAL_DEFAULT_VALUE);
	}
}
