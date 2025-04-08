package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.ErrorMessageListUtils;
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
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(LogAndSaveSendError.class, variables.getStartTask());

		String correlationKey = variables.getTarget().getCorrelationKey();
		String errorMessage = (String) execution.getVariableLocal(ConstantsPing.getBpmnExecutionVariableErrorMessage());

		ErrorMessageListUtils.add(errorMessage, execution, correlationKey);
		variables.setInteger(ConstantsPing.getBpmnExecutionVariableUploadedBytes(correlationKey), 0);
		variables.setLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(correlationKey), 0L);
		logger.debug("Saved error when trying to send ping message. Error message: {}", errorMessage);
	}
}
