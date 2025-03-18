package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.ErrorMessageListUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
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
		String correlationKey = variables.getTarget().getCorrelationKey();
		String statusCode = (String) execution.getVariableLocal(ConstantsPing.getBpmnExecutionVariableStatusCode());
		String errorMessage = (String) execution.getVariableLocal(ConstantsPing.getBpmnExecutionVariableErrorMessage());

		variables.setString(ConstantsPing.getBpmnExecutionVariableStatusCode(correlationKey), statusCode);
		ErrorMessageListUtils.add(errorMessage, execution);
		variables.setInteger(ConstantsPing.getBpmnExecutionVariableUploadedBytes(correlationKey), 0);
		variables.setLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(correlationKey), 0L);
	}
}
