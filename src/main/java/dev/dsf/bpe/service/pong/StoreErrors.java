package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessErrors;
import dev.dsf.bpe.service.AbstractService;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreErrors extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(StoreErrors.class);

	public StoreErrors(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecuteWithErrorHandling(DelegateExecution execution, Variables variables) throws BpmnError
	{
		Task startTask = variables.getStartTask();
		logger.debug("Storing errors...");

		ProcessErrors errors = ErrorListUtils.getErrorList(execution);
		PingStatusGenerator.updatePongStatusOutput(startTask, errors.getEntries());

		CodeSystem.DsfPingStatus.Code status = (CodeSystem.DsfPingStatus.Code) variables
				.getVariable(ExecutionVariables.statusCode.name());
		PingStatusGenerator.updatePongStatusOutput(startTask, status);

		variables.updateTask(startTask);
		logger.debug("Stored errors in task: " + startTask.getIdElement().getValue());
	}
}
