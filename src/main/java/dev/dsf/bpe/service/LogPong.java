package dev.dsf.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Variables;

public class LogPong extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogPong.class);

	private final PingStatusGenerator responseGenerator;

	public LogPong(ProcessPluginApi api, PingStatusGenerator responseGenerator)
	{
		super(api);

		this.responseGenerator = responseGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(responseGenerator, "responseGenerator");
	}


	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Target target = variables.getTarget();

		logger.info("PONG from {} (endpoint: {})", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue());

		Task mainTask = variables.getStartTask();
		mainTask.addOutput(responseGenerator.createPingStatusOutput(target,
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_RECEIVED));
		variables.updateTask(mainTask);

		Targets targets = variables.getTargets();
		targets = targets.removeByEndpointIdentifierValue(target.getEndpointIdentifierValue());
		variables.setTargets(targets);
	}
}
