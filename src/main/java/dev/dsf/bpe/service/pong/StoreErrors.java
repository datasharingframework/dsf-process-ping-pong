package dev.dsf.bpe.service.pong;

import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.ErrorMessageListUtils;
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
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Task startTask = variables.getStartTask();
		PingPongLogger logger = new PingPongLogger(StoreErrors.class, startTask);
		logger.debug("Storing errors...");

		List<String> errors = ErrorMessageListUtils.getErrorMessageList(execution);
		PingStatusGenerator.updatePongStatusOutput(startTask, errors);

		if (!errors.isEmpty())
		{
			PingStatusGenerator.updatePongStatusOutput(startTask, ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR);
		}
		else
		{
			PingStatusGenerator.updatePongStatusOutput(startTask,
					ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_COMPLETED);
		}

		variables.updateTask(startTask);
		logger.debug("Stored errors in task: " + startTask.getIdElement().getValue());
	}
}
