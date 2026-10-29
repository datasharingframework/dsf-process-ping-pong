package dev.dsf.bpe.service.ping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class ClearPingStatus implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(ClearPingStatus.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Clearing ping status in preparation of sending another ping message without reference.");

		variables.setJsonVariableLocal(ExecutionVariables.error.name(), null);
		variables.setJsonVariableLocal(ExecutionVariables.statusCode.name(), null);
		variables.setStringLocal(ExecutionVariables.rawHttpStatus.name(), null);
	}
}
