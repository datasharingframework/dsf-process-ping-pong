package dev.dsf.bpe.service.pong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class ClearPongStatus implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(ClearPongStatus.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Clearing pong status in preparation of sending another pong message without reference.");

		variables.setJsonVariable(ExecutionVariables.error.name(), null);
		variables.setJsonVariable(ExecutionVariables.statusCode.name(), null);
		variables.setString(ExecutionVariables.rawHttpStatus.name(), null);
	}
}
