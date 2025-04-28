package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveNoResponse extends AbstractServiceDelegate
{
	public LogAndSaveNoResponse(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(LogAndSaveNoResponse.class, variables.getStartTask());
		logger.debug("Saving no response to process execution...");

		Target target = variables.getTarget();
		String pongMissingMessage = "No PONG received from endpoint '" + target.getEndpointIdentifierValue() + "'";
		logger.info(pongMissingMessage);

		String correlationKey = target.getCorrelationKey();
		delegateExecution.removeVariable("statusCode");
		ProcessError error = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_PONG_MESSAGE_TIMEOUT_TIMER_CATCH_EVENT,
				"Awaiting pong message", ConstantsPing.POTENTIAL_FIX_URL_DUMMY, pongMissingMessage);
		ErrorListUtils.add(error, delegateExecution, correlationKey);

		logger.debug("Saved '{}' to process execution for correlation key '{}'",
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR, correlationKey);
	}
}
