package dev.dsf.bpe.service.pong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class SavePingInstallOrAllowlistError implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SavePingInstallOrAllowlistError.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		String correlationKey = variables.getTarget().getCorrelationKey();
		ProcessError error = new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
				CodeSystem.DsfPingError.Concept.REMOTE_ORG_MISSING_PING_INSTALL_OR_LOCAL_ORG_NOT_IN_REMOTE_ALLOWLIST,
				null);

		ErrorListUtils.add(error, variables, correlationKey);

		variables.setBoolean(ExecutionVariables.sendPong.name(), false);

		logger.debug(
				"Sending a message with a reference failed with HTTP 403, sending a message without a reference also failed with HTTP 403: {}",
				error.concept().getDisplay());
	}
}
