package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveSendError extends AbstractServiceDelegate
{
	public LogAndSaveSendError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError
	{
		PingPongLogger logger = new PingPongLogger(LogAndSaveSendError.class, variables.getStartTask());

		String correlationKey = variables.getTarget().getCorrelationKey();
		ProcessError error = ProcessError
				.parse((String) execution.getVariableLocal(ExecutionVariables.ERROR.getValue()));
		ErrorListUtils.add(error, execution, correlationKey);
		variables.setLong(ExecutionVariables.UPLOADED_BYTES.correlatedValue(correlationKey), 0L);
		variables.setLong(ExecutionVariables.UPLOADED_DURATION_MILLIS.correlatedValue(correlationKey), 0L);
		logger.debug("Saved error when trying to send ping message. Error message: {}", error.message());
	}
}
