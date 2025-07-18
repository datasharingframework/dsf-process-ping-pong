package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
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
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Task startTask = variables.getStartTask();
		PingPongLogger logger = new PingPongLogger(LogAndSaveUploadErrorPing.class, startTask);

		ProcessError error = ProcessError
				.parse(variables.getString(ConstantsPing.BPMN_EXECUTION_VARIABLE_RESOURCE_UPLOAD_ERROR));

		logger.info("Error while storing binary resource for download: {}", error.message());
	}
}
