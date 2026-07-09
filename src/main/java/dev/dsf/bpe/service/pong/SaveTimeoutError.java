package dev.dsf.bpe.service.pong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class SaveTimeoutError implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SaveTimeoutError.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Storing timeout error...");

		String correlationKey = variables.getTarget().getCorrelationKey();

		ProcessError error = new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
				CodeSystem.DsfPingError.Concept.CLEANUP_MESSAGE_TIMEOUT, null);

		ErrorListUtils.add(error, variables, correlationKey);

		logger.debug("Stored timeout error: {}", error.concept().getDisplay());
	}
}
