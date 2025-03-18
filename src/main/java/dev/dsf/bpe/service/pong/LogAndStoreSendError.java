package dev.dsf.bpe.service.pong;

import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndStoreSendError extends AbstractServiceDelegate
{
	public LogAndStoreSendError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(LogAndSaveAndStoreError.class, variables.getStartTask());

		String statusCode = variables.getString(ConstantsPing.getBpmnExecutionVariableStatusCode());
		String errorMessage = variables.getString(ConstantsPing.getBpmnExecutionVariableErrorMessage());
		Target target = variables.getTarget();

		Task startTask = variables.getStartTask();
		Task.TaskOutputComponent pongStatus = PingStatusGenerator.createPongStatusOutput(variables.getTarget(),
				statusCode, List.of(errorMessage));
		startTask.addOutput(pongStatus);
		variables.updateTask(startTask);
		logger.error("Error {} when trying to download resource from {}: {}", statusCode,
				target.getEndpointIdentifierValue(), errorMessage);
	}
}
