package dev.dsf.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsBase;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.delegate.AbstractServiceDelegate;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.fhir.authorization.read.ReadAccessHelper;
import dev.dsf.fhir.client.FhirWebserviceClientProvider;
import dev.dsf.fhir.task.TaskHelper;
import dev.dsf.fhir.variables.FhirResourceValues;
import dev.dsf.fhir.variables.Target;
import dev.dsf.fhir.variables.Targets;

public class LogNoResponse extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogNoResponse.class);

	private final PingStatusGenerator responseGenerator;
	private final ErrorMailService errorLogger;

	public LogNoResponse(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, PingStatusGenerator responseGenerator, ErrorMailService errorLogger)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.responseGenerator = responseGenerator;
		this.errorLogger = errorLogger;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(responseGenerator, "responseGenerator");
		Objects.requireNonNull(errorLogger, "errorLogger");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getLeadingTaskFromExecutionVariables(execution);

		Targets targets = (Targets) execution.getVariable(ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS);
		targets.getEntries().forEach(t -> logAndAddResponseToTask(task, t));

		execution.setVariable(ConstantsBase.BPMN_EXECUTION_VARIABLE_LEADING_TASK, FhirResourceValues.create(task));
	}

	private void logAndAddResponseToTask(Task task, Target target)
	{
		logger.warn("PONG from organization {} (endpoint {}) missing", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue());

		task.addOutput(responseGenerator.createPingStatusOutput(target,
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_MISSING));
		errorLogger.pongMessageNotReceived(task.getIdElement(), target);
	}
}
