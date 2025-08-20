package dev.dsf.bpe.service.ping;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.fhir.client.FhirWebserviceClient;
import jakarta.ws.rs.WebApplicationException;

public class CheckPingTaskStatus extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(CheckPingTaskStatus.class);

	public CheckPingTaskStatus(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		logger.debug("Checking status of ping task...");

		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();

		String taskId = (String) delegateExecution.getVariableLocal(ExecutionVariables.pingTaskId.name());

		Objects.requireNonNull(taskId);
		FhirWebserviceClient fhirWebserviceClient = api.getFhirWebserviceClientProvider()
				.getWebserviceClient(target.getEndpointUrl());
		ProcessError error;
		try
		{
			Task pingTask = fhirWebserviceClient.withRetry(3, 1000).read(Task.class, taskId);
			error = switch (pingTask.getStatus())
			{
				case REQUESTED, INPROGRESS, FAILED, COMPLETED -> new ProcessError(CodeSystem.DsfPingProcesses.Code.PING,
						CodeSystem.DsfPingProcessSteps.Code.CHECK_PING_TASK_STATUS, "Awaiting pong message", null,
						"Pong message timed out. Status of ping task resource with id " + taskId + " from "
								+ target.getEndpointUrl() + " is " + pingTask.getStatus());
				default -> new ProcessError(CodeSystem.DsfPingProcesses.Code.PING,
						CodeSystem.DsfPingProcessSteps.Code.CHECK_PING_TASK_STATUS, "Awaiting pong message", null,
						"Pong message timed out. Status of ping task resource with id " + taskId + " from "
								+ target.getEndpointUrl() + " is " + pingTask.getStatus()
								+ ". Unexpected status. Should be either of " + Task.TaskStatus.REQUESTED + ", "
								+ Task.TaskStatus.INPROGRESS + ", " + Task.TaskStatus.COMPLETED + " or "
								+ Task.TaskStatus.FAILED);
			};
		}
		catch (WebApplicationException e)
		{
			error = new ProcessError(CodeSystem.DsfPingProcesses.Code.PING,
					CodeSystem.DsfPingProcessSteps.Code.CHECK_PING_TASK_STATUS,
					"Pong message timed out. Error when retrieving status of ping task resource with id " + taskId
							+ " from " + target.getEndpointUrl(),
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP, e.getMessage());
		}
		ErrorListUtils.add(error, delegateExecution, correlationKey);
		logger.debug("Saved '{}' to process execution for correlation key '{}'",
				CodeSystem.DsfPing.Code.ERROR.getValue(), correlationKey);
	}
}
