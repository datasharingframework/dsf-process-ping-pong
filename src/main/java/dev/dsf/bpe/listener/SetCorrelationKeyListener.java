package dev.dsf.bpe.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ExecutionListener;
import dev.dsf.bpe.v2.constants.BpmnExecutionVariables;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class SetCorrelationKeyListener implements ExecutionListener
{
	private static final Logger logger = LoggerFactory.getLogger(SetCorrelationKeyListener.class);

	@Override
	public void notify(ProcessPluginApi processPluginApi, Variables variables) throws Exception
	{
		logger.debug("Setting correlation key for subprocess instance {}", variables.getActivityInstanceId());
		Target target = variables.getTarget();

		variables.setStringLocal(BpmnExecutionVariables.CORRELATION_KEY, target.getCorrelationKey());
	}
}
