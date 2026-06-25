package dev.dsf.bpe.service.pong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class LogAndSaveUploadErrorPong implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveUploadErrorPong.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		ProcessError error = variables.getVariable(ExecutionVariables.resourceUploadError.name());
		ErrorListUtils.add(error, variables);

		ProcessError errorRemote = variables
				.getVariable(ExecutionVariables.resourceUploadErrorRemote.name());
		ErrorListUtils.addRemote(errorRemote, variables);

		logger.info("Error while storing binary resource for download: {}", error.concept().getDisplay());
	}
}
