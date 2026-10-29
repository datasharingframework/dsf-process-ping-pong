package dev.dsf.bpe.service.pong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class LogAndSaveSendErrorPong implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveSendErrorPong.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();

		ProcessError errorLocal = variables.getVariable(ExecutionVariables.errorLocal.name());
		ErrorListUtils.add(errorLocal, variables, correlationKey);

		ProcessError errorRemote = variables.getVariable(ExecutionVariables.errorRemote.name());
		ErrorListUtils.addRemote(errorRemote, variables);

		variables.setBoolean(ExecutionVariables.sendPong.name(), true);

		logger.debug("Error while sending a message to endpoint {} of organization {}: {}",
				target.getEndpointIdentifierValue(), target.getOrganizationIdentifierValue(),
				errorLocal.concept().getDisplay());
	}
}
