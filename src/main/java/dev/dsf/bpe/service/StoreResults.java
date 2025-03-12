package dev.dsf.bpe.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreResults extends AbstractServiceDelegate implements InitializingBean
{
	private final ErrorMailService errorMailService;

	public StoreResults(ProcessPluginApi api, ErrorMailService errorMailService)
	{
		super(api);

		this.errorMailService = errorMailService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(errorMailService, "errorMailService");
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Task task = variables.getStartTask();
		Targets targets = variables.getTargets();

		targets.getEntries().stream().sorted(Comparator.comparing(Target::getEndpointIdentifierValue)).forEach(target ->
		{
			String correlationKey = target.getCorrelationKey();

			String statusCode = variables.getString(ConstantsPing.getBpmnExecutionVariableStatusCode(correlationKey));
			if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE.equals(statusCode)
					|| ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED.equals(statusCode))
			{
				String errorMessage = variables
						.getString(ConstantsPing.getBpmnExecutionVariableErrorMessage(correlationKey));
				task.addOutput(PingStatusGenerator.createPingStatusOutput(target, statusCode, List.of(errorMessage)));

				if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE.equals(statusCode))
					errorMailService.endpointNotReachableForPing(task.getIdElement(), target, errorMessage);
				else if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED.equals(statusCode))
					errorMailService.endpointReachablePingForbidden(task.getIdElement(), target, errorMessage);
			}
			else
			{
				task.addOutput(PingStatusGenerator.createPingStatusOutput(target, statusCode));

				if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_MISSING.equals(statusCode))
					errorMailService.pongMessageNotReceived(task.getIdElement(), target);
			}
		});

		// TODO only send one combined status mail

		variables.updateTask(task);
	}
}
