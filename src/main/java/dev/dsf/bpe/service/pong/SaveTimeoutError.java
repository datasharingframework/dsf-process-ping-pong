package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class SaveTimeoutError extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(SaveTimeoutError.class);

	public SaveTimeoutError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError
	{
		Target target = variables.getTarget();
		logger.debug("Storing timeout error...");

		ProcessError error = new ProcessError(CodeSystem.DsfPingProcesses.Code.PONG,
				CodeSystem.DsfPingProcessSteps.Code.CLEANUP_TIMER_CATCH_EVENT,
				"Waiting for cleanup message from " + target.getOrganizationIdentifierValue(), null,
				"Timeout while waiting for cleanup message");

		ErrorListUtils.add(error, execution);

		logger.debug("Stored timeout error: {}", error.message());
	}
}
