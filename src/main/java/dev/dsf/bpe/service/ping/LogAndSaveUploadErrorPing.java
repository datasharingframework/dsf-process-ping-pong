package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveUploadErrorPing extends AbstractServiceDelegate
{
	public LogAndSaveUploadErrorPing(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError
	{
		Task startTask = variables.getStartTask();
		PingPongLogger logger = new PingPongLogger(LogAndSaveUploadErrorPing.class, startTask);

		ProcessError error = ProcessError
				.parse(variables.getString(ExecutionVariables.RESOURCE_UPLOAD_ERROR.getValue()));

		logger.info("Error while storing binary resource for download: {}", error.message());
	}
}
