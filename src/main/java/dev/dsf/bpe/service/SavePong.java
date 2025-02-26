package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class SavePong extends AbstractServiceDelegate
{
	public SavePong(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();
		delegateExecution.removeVariable("statusCode");
		variables.setString("statusCode_" + correlationKey, ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_RECEIVED);

		//TODO: add other information: downloaded-bytes, downloaded-duration-millis, error-message
	}
}
