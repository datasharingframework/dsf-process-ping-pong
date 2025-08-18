package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveAndStoreError extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveAndStoreError.class);

	public LogAndSaveAndStoreError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		Target target = variables.getTarget();
		Task startTask = variables.getStartTask();

		ProcessError error = (ProcessError) delegateExecution
				.getVariableLocal(ExecutionVariables.RESOURCE_DOWNLOAD_ERROR.getValue());

		ErrorListUtils.add(error, delegateExecution);
		PingStatusGenerator.updatePongStatusOutput(startTask,
				ErrorListUtils.getErrorList(delegateExecution).getEntries());
		variables.updateTask(startTask);

		logger.info("Error while trying to download resource from {}: {}", target.getEndpointUrl(), error.message());
	}
}
