package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.ErrorMessageListUtils;
import dev.dsf.bpe.util.task.input.generator.ErrorMessageGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveSendError extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveSendError.class);

	public LogAndSaveSendError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		String statusCode = (String) delegateExecution.getVariableLocal("statusCode");
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();
		variables.setString(ConstantsPing.BPMN_EXECUTION_VARIABLE_STATUS_CODE + "_" + correlationKey, statusCode);
		logger.info("Request to {} resulted in status {}", target.getEndpointUrl(), statusCode);
	}
}
