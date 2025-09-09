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
import dev.dsf.bpe.variables.codesystem.dsfpingstatus.CodeValueImpl;
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
		try
		{
			Task pingTask = fhirWebserviceClient.withRetry(3, 1000).read(Task.class, taskId);
			ProcessError error = switch (pingTask.getStatus())
			{
				case COMPLETED -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_STATUS_COMPLETED, null);
				case FAILED -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_STATUS_FAILED, null);
				case INPROGRESS -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_STATUS_IN_PROGRESS, null);
				case REQUESTED -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_STATUS_REQUESTED, null);
				default -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_STATUS_UNEXPECTED, null);
			};
			ErrorListUtils.add(error, delegateExecution, correlationKey);
		}
		catch (WebApplicationException e)
		{
			ProcessError error = getProcessError(e);
			ErrorListUtils.add(error, delegateExecution, correlationKey);
		}
		finally
		{
			variables.setVariable(ExecutionVariables.statusCode.correlatedValue(correlationKey),
					new CodeValueImpl(CodeSystem.DsfPingStatus.Code.PONG_MISSING));
		}

		logger.debug("Saved '{}' to process execution for correlation key '{}'",
				CodeSystem.DsfPing.Code.ERROR.getValue(), correlationKey);
	}

	private static ProcessError getProcessError(WebApplicationException e)
	{
		int statusCode = e.getResponse().getStatus();
		return switch (statusCode)
		{
			case 401 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_401,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 403 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_403,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 500 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_500,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 502 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_502,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			default -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_UNEXPECTED,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
		};
	}
}
