package dev.dsf.bpe.service.pong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class LogReferenceResolutionSuccess implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(LogReferenceResolutionSuccess.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Target target = variables.getTarget();

		variables.setBoolean(ExecutionVariables.sendPong.name(), true);

		logger.info("Reference was successfully resolved on endpoint {} for organization {}",
				target.getEndpointIdentifierValue(), target.getOrganizationIdentifierValue());
	}
}
