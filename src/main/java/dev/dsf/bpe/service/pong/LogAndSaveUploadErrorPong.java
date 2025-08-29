package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveUploadErrorPong extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveUploadErrorPong.class);

	public LogAndSaveUploadErrorPong(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError
	{
		ProcessError error = (ProcessError) variables.getVariable(ExecutionVariables.resourceUploadError.name());
		ErrorListUtils.add(error, execution);

		ProcessError errorRemote = (ProcessError) variables
				.getVariable(ExecutionVariables.resourceUploadErrorRemote.name());
		ErrorListUtils.addRemote(errorRemote, execution);

		logger.info("Error while storing binary resource for download: {}", error.concept().getDisplay());
	}
}
