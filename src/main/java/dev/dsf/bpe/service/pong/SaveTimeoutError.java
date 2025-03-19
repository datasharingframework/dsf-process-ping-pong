package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.ErrorMessageListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class SaveTimeoutError extends AbstractServiceDelegate
{
	public SaveTimeoutError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Task startTask = variables.getStartTask();
		PingPongLogger logger = new PingPongLogger(SaveTimeoutError.class, startTask);
		logger.debug("Storing timeout error...");

		String errorMessage = ConstantsPing.PONG_ERROR_MESSAGE_CLEANUP_TIMEOUT;

		ErrorMessageListUtils.add(errorMessage, execution);

		logger.debug("Stored timeout error: {}", errorMessage);
	}
}
