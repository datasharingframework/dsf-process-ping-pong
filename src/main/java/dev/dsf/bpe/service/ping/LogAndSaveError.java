package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveError extends AbstractServiceDelegate
{
	public LogAndSaveError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		PingPongLogger logger = new PingPongLogger(LogAndSaveError.class, variables.getStartTask());
		Target target = variables.getTarget();

		ProcessError error = (ProcessError) delegateExecution
				.getVariableLocal(ExecutionVariables.RESOURCE_DOWNLOAD_ERROR.getValue());

		ErrorListUtils.add(error, delegateExecution, target.getCorrelationKey());

		logger.info("Error while trying to download resource from {}: {}", target.getEndpointUrl(), error.message());
	}
}
