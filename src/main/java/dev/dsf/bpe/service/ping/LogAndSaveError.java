package dev.dsf.bpe.service.ping;

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

public class LogAndSaveError implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveError.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Target target = variables.getTarget();

		ProcessError error = variables
				.getVariableLocal(ExecutionVariables.resourceDownloadError.name());

		ErrorListUtils.add(error, variables, target.getCorrelationKey());

		logger.info("Error while trying to download resource from {}: {}", target.getEndpointUrl(),
				error.concept().getDisplay());
	}
}
