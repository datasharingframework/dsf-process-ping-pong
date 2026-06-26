package dev.dsf.bpe.service.pong;

import java.util.Map;
import java.util.Objects;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessErrors;
import dev.dsf.bpe.mail.AggregateErrorMailService;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class StoreErrors implements ServiceTask, InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(StoreErrors.class);

	private final AggregateErrorMailService errorMailService;

	public StoreErrors(AggregateErrorMailService errorMailService)
	{
		this.errorMailService = errorMailService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(errorMailService, "errorMailService");
	}

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Task startTask = variables.getStartTask();
		logger.debug("Storing errors...");

		ProcessErrors errors = ErrorListUtils.getErrorList(variables);
		PingStatusGenerator.updatePongStatusOutput(startTask, errors.getEntries());

		CodeSystem.DsfPingStatus.Code status = variables
				.getVariable(ExecutionVariables.statusCode.name());
		PingStatusGenerator.updatePongStatusOutput(startTask, status);

		variables.updateTask(startTask);

		Target target = variables.getTarget();
		errorMailService.send(startTask.getIdElement(), Map.of(target, errors.getEntries()));

		logger.debug("Stored errors in task: " + startTask.getIdElement().getValue());
	}
}
