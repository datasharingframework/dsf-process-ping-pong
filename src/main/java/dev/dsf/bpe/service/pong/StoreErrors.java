package dev.dsf.bpe.service.pong;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
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
	private final PingStatusGenerator pingStatusGenerator;

	public StoreErrors(AggregateErrorMailService errorMailService, PingStatusGenerator pingStatusGenerator)
	{
		this.errorMailService = errorMailService;
		this.pingStatusGenerator = pingStatusGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(errorMailService, "errorMailService");
	}

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Task startTask = variables.getStartTask();
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();
		String resourceVersion = api.getProcessPluginDefinition().getResourceVersion();
		logger.debug("Storing errors...");

		List<ProcessError> localProcessErrors = ErrorListUtils.getErrorList(variables).getEntries();
		ProcessError.toTaskOutput(localProcessErrors, resourceVersion).forEach(startTask::addOutput);

		ProcessErrors targetErrors = ErrorListUtils.getErrorList(variables, correlationKey);
		pingStatusGenerator.updatePongStatusOutput(startTask, targetErrors.getEntries());

		CodeSystem.DsfPingStatus.Code status = variables.getVariable(ExecutionVariables.statusCode.name());
		pingStatusGenerator.updatePongStatusOutput(startTask, status);

		variables.updateTask(startTask);

		errorMailService.send(startTask.getIdElement(), localProcessErrors, Map.of(target, targetErrors.getEntries()));

		logger.debug("Stored errors in task: " + startTask.getIdElement().getValue());
	}
}
