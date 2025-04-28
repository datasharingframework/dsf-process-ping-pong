package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveUploadErrorPong extends AbstractServiceDelegate
{
	public LogAndSaveUploadErrorPong(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Task startTask = variables.getStartTask();
		PingPongLogger logger = new PingPongLogger(LogAndSaveUploadErrorPong.class, startTask);

		String errorMessage = variables.getString(ConstantsPing.BPMN_EXECUTION_VARIABLE_RESOURCE_UPLOAD_ERROR);
		ProcessError error = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PONG,
				ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_GENERATE_AND_STORE_RESOURCE,
				"Storing binary resource for download", ConstantsPing.POTENTIAL_FIX_URL_DUMMY, errorMessage);

		ErrorListUtils.add(error, execution);

		logger.info("Error while storing binary resource for download: {}", errorMessage);
	}
}
