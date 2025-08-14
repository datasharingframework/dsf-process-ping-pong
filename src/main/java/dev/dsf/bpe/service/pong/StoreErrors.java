package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ProcessErrors;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreErrors extends AbstractServiceDelegate
{
	public StoreErrors(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError
	{
		Task startTask = variables.getStartTask();
		PingPongLogger logger = new PingPongLogger(StoreErrors.class, startTask);
		logger.debug("Storing errors...");

		ProcessErrors errors = ErrorListUtils.getErrorList(execution);
		PingStatusGenerator.updatePongStatusOutput(startTask, errors.getEntries());

		if (!errors.isEmpty())
		{
			PingStatusGenerator.updatePongStatusOutput(startTask, CodeSystem.DsfPingStatus.Code.ERROR);
		}
		else
		{
			PingStatusGenerator.updatePongStatusOutput(startTask, CodeSystem.DsfPingStatus.Code.COMPLETED);
		}

		variables.updateTask(startTask);
		logger.debug("Stored errors in task: " + startTask.getIdElement().getValue());
	}
}
