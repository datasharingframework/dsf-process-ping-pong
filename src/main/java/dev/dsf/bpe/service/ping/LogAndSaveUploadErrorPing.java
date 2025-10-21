package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.service.AbstractService;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveUploadErrorPing extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveUploadErrorPing.class);

	public LogAndSaveUploadErrorPing(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecuteWithErrorHandling(DelegateExecution execution, Variables variables) throws BpmnError
	{
		ProcessError error = (ProcessError) variables.getVariable(ExecutionVariables.resourceUploadError.name());

		ErrorListUtils.add(error, execution);

		logger.info("Error while storing binary resource for download: {}", error.concept().getDisplay());
	}

	@Override
	protected void handleException(DelegateExecution execution, Variables variables, Exception exception)
			throws Exception
	{
		logger.error("Unexpected error while storing binary resource for download.", exception);
		throw exception;
	}
}
