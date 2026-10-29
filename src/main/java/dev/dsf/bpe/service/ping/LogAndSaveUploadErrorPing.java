package dev.dsf.bpe.service.ping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.error.ServiceTaskErrorHandler;
import dev.dsf.bpe.v2.error.impl.DefaultServiceTaskErrorHandler;
import dev.dsf.bpe.v2.variables.Variables;

public class LogAndSaveUploadErrorPing implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveUploadErrorPing.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		ProcessError error = variables.getVariable(ExecutionVariables.resourceUploadError.name());

		ErrorListUtils.add(error, variables);

		logger.info("Error while storing binary resource for download: {}", error.concept().getDisplay());
	}

	@Override
	public ServiceTaskErrorHandler getErrorHandler()
	{
		return new DefaultServiceTaskErrorHandler()
		{
			@Override
			public Exception handleException(ProcessPluginApi api, Variables variables, Exception exception)
			{
				return super.handleException(api, variables, exception);
			}
		};
	}
}
