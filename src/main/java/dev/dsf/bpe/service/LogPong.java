package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class LogPong extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogPong.class);

	public LogPong(ProcessPluginApi api)
	{
		super(api);

	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Target target = variables.getTarget();

		logger.info("PONG from {} (endpoint: {})", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue());

		execution.removeVariable("statusCode");
		variables.setString("statusCode_" + target.getCorrelationKey(),
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_RECEIVED);
	}
}
