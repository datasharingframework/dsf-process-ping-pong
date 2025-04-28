package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveAndStoreError extends AbstractServiceDelegate
{
	public LogAndSaveAndStoreError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(LogAndSaveAndStoreError.class, variables.getStartTask());
		Target target = variables.getTarget();
		Task startTask = variables.getStartTask();

		String errorMessage = variables.getString(ConstantsPing.BPMN_EXECUTION_VARIABLE_RESOURCE_DOWNLOAD_ERROR);
		ProcessError error = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PONG,
				ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_DOWNLOAD_RESOURCE_AND_MEASURE_SPEED,
				"Downloading binary resource from " + target.getEndpointUrl(), ConstantsPing.POTENTIAL_FIX_URL_DUMMY,
				errorMessage);

		ErrorListUtils.add(error, delegateExecution);
		PingStatusGenerator.updatePongStatusOutput(startTask, ErrorListUtils.getErrorMessageList(delegateExecution));
		variables.updateTask(startTask);

		logger.info("Error while trying to download resource from {}: {}", target.getEndpointUrl(), errorMessage);
	}
}
