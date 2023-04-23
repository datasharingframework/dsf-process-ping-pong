package dev.dsf.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Variables;

public class LogNoResponse extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogNoResponse.class);

	private final PingStatusGenerator responseGenerator;
	private final ErrorMailService errorMailService;

	public LogNoResponse(ProcessPluginApi api, PingStatusGenerator responseGenerator, ErrorMailService errorMailService)
	{
		super(api);

		this.responseGenerator = responseGenerator;
		this.errorMailService = errorMailService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(errorMailService, "errorLogger");
	}


	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Task mainTask = variables.getMainTask();

		Targets targets = variables.getTargets();
		targets.getEntries().forEach(t -> logAndAddResponseToTask(mainTask, t));

		variables.updateTask(mainTask);
	}

	private void logAndAddResponseToTask(Task task, Target target)
	{
		logger.warn("PONG from organization {} (endpoint {}) missing", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue());

		task.addOutput(responseGenerator.createPingStatusOutput(target,
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_MISSING));
		errorMailService.pongMessageNotReceived(task.getIdElement(), target);
	}
}
