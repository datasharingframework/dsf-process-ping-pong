package dev.dsf.bpe.service.ping;

import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.fhir.client.FhirWebserviceClient;
import jakarta.ws.rs.WebApplicationException;

public class CheckPingTaskStatus extends AbstractServiceDelegate
{
	public CheckPingTaskStatus(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(CheckPingTaskStatus.class, variables.getStartTask());
		logger.debug("Checking status of ping task...");

		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();

		IdType taskId = (IdType) delegateExecution.getVariableLocal(ConstantsPing.BPMN_EXECUTION_VARIABLE_PING_TASK_ID);

		Objects.requireNonNull(taskId);
		FhirWebserviceClient fhirWebserviceClient = api.getFhirWebserviceClientProvider()
				.getWebserviceClient(target.getEndpointUrl());
		ProcessError error;
		try
		{

			Task pingTask = fhirWebserviceClient.withRetry(3, 1000).read(Task.class, taskId.getIdPart());
			switch (pingTask.getStatus())
			{
				case REQUESTED -> error = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CHECK_PING_TASK_STATUS,
						"Awaiting pong message", ConstantsPing.POTENTIAL_FIX_URL_DUMMY,
						"Pong message timed out. Status of ping task resource with id " + taskId.getIdPart() + " from "
								+ target.getEndpointUrl() + " is " + pingTask.getStatus());
				case INPROGRESS -> error = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CHECK_PING_TASK_STATUS,
						"Awaiting pong message", ConstantsPing.POTENTIAL_FIX_URL_DUMMY,
						"Pong message timed out. Status of ping task resource with id " + taskId.getIdPart() + " from "
								+ target.getEndpointUrl() + " is " + pingTask.getStatus());
				case FAILED -> error = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CHECK_PING_TASK_STATUS,
						"Awaiting pong message", ConstantsPing.POTENTIAL_FIX_URL_DUMMY,
						"Pong message timed out. Status of ping task resource with id " + taskId.getIdPart() + " from "
								+ target.getEndpointUrl() + " is " + pingTask.getStatus());
				case COMPLETED -> error = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CHECK_PING_TASK_STATUS,
						"Awaiting pong message", ConstantsPing.POTENTIAL_FIX_URL_DUMMY,
						"Pong message timed out. Status of ping task resource with id " + taskId.getIdPart() + " from "
								+ target.getEndpointUrl() + " is " + pingTask.getStatus());
				default -> error = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CHECK_PING_TASK_STATUS,
						"Awaiting pong message", ConstantsPing.POTENTIAL_FIX_URL_DUMMY,
						"Pong message timed out. Status of ping task resource with id " + taskId.getIdPart() + " from "
								+ target.getEndpointUrl() + " is " + pingTask.getStatus()
								+ ". Unexpected status. Should be either of " + Task.TaskStatus.REQUESTED + ", "
								+ Task.TaskStatus.INPROGRESS + ", " + Task.TaskStatus.COMPLETED + " or "
								+ Task.TaskStatus.FAILED);
			}


		}
		catch (WebApplicationException e)
		{
			error = new ProcessError(ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PING,
					ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CHECK_PING_TASK_STATUS,
					"Pong message timed out. Retrieving ping task resource with id " + taskId.getIdPart() + " from "
							+ target.getEndpointUrl(),
					ConstantsPing.POTENTIAL_FIX_URL_DUMMY, e.getMessage());
		}
		ErrorListUtils.add(error, delegateExecution, correlationKey);
		logger.debug("Saved '{}' to process execution for correlation key '{}'",
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR, correlationKey);
	}
}
