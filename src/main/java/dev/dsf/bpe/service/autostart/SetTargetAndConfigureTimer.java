package dev.dsf.bpe.service.autostart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class SetTargetAndConfigureTimer implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SetTargetAndConfigureTimer.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		String timerInterval = getTimerInterval(api, variables);
		logger.debug("Setting variable '{}' to {}", ExecutionVariables.timerInterval.name(), timerInterval);

		variables.setString(ExecutionVariables.timerInterval.name(), timerInterval);
		variables.setTarget(
				variables.createTarget(api.getOrganizationProvider().getLocalOrganizationIdentifierValue().get(),
						api.getEndpointProvider().getLocalEndpointIdentifierValue().get(),
						api.getEndpointProvider().getLocalEndpointAddress()));
	}

	private String getTimerInterval(ProcessPluginApi api, Variables variables)
	{
		return api.getTaskHelper()
				.getFirstInputParameterStringValue(variables.getStartTask(), CodeSystem.DsfPing.URL,
						CodeSystem.DsfPing.Code.TIMER_INTERVAL.getValue())
				.orElse(ConstantsPing.TIMER_INTERVAL_DEFAULT_VALUE);
	}
}
