package dev.dsf.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsBase;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.delegate.AbstractServiceDelegate;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.fhir.authorization.read.ReadAccessHelper;
import dev.dsf.fhir.client.FhirWebserviceClientProvider;
import dev.dsf.fhir.task.TaskHelper;
import dev.dsf.fhir.variables.Target;
import dev.dsf.fhir.variables.Targets;
import dev.dsf.fhir.variables.TargetsValues;

public class LogPong extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogPong.class);

	private final PingStatusGenerator responseGenerator;

	public LogPong(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, PingStatusGenerator responseGenerator)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.responseGenerator = responseGenerator;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(responseGenerator, "responseGenerator");
	}

	@Override
	public void doExecute(DelegateExecution execution)
	{
		Target target = getTarget(execution);

		logger.info("PONG from {} (endpoint: {})", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue());

		Task leading = getLeadingTaskFromExecutionVariables(execution);
		leading.addOutput(responseGenerator.createPingStatusOutput(target,
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_RECEIVED));
		updateLeadingTaskInExecutionVariables(execution, leading);

		Targets targets = (Targets) execution.getVariable(ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS);
		targets = targets.removeByEndpointIdentifierValue(target.getEndpointIdentifierValue());
		execution.setVariable(ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(targets));
	}

	private Target getTarget(DelegateExecution execution)
	{
		return (Target) execution.getVariable(ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET);
	}
}
