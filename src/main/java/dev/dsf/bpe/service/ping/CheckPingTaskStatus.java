package dev.dsf.bpe.service.ping;

import java.time.Duration;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.client.dsf.DelayStrategy;
import dev.dsf.bpe.v2.client.dsf.DsfClient;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.error.ServiceTaskErrorHandler;
import dev.dsf.bpe.v2.error.impl.DefaultServiceTaskErrorHandler;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;
import jakarta.ws.rs.WebApplicationException;

public class CheckPingTaskStatus implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(CheckPingTaskStatus.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Checking status of ping task...");

		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();

		// Do not use getVariableLocal() here. CheckPingTaskStatus gets executed in a child execution of the one that
		// stored the variable. GetVariableLocal() will only look in the child execution's variables and find nothing.
		// GetVariable() or the DSF API's getString() does look for values in the parent execution's variables.
		String taskId = variables.getString(ExecutionVariables.pingTaskId.name());

		try
		{
			if (taskId != null)
			{
				DsfClient dsfClient = api.getDsfClientProvider()
						.getByEndpointUrl(target.getEndpointUrl());

				Task pingTask = dsfClient.withRetry(3, DelayStrategy.constant(Duration.ofSeconds(1))).read(Task.class, taskId);
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
				ErrorListUtils.add(error, variables, correlationKey);
			}
		}
		catch (WebApplicationException e)
		{
			ProcessError error = getProcessError(e);
			ErrorListUtils.add(error, variables, correlationKey);
		}
		finally
		{
			variables.setJsonVariable(ExecutionVariables.statusCode.correlatedValue(correlationKey),
					CodeSystem.DsfPingStatus.Code.PONG_MISSING);
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
			case 407 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_407,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 500 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_500,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 502 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_502,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 503 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_503,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 504 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_504,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			default -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
					CodeSystem.DsfPingError.Concept.RESPONSE_MESSAGE_TIMEOUT_HTTP_UNEXPECTED,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
		};
	}

	@Override
	public ServiceTaskErrorHandler getErrorHandler()
	{
		return new DefaultServiceTaskErrorHandler()
		{
			@Override
			public Exception handleException(ProcessPluginApi processPluginApi, Variables variables, Exception e)
			{
				logger.error("Unexpected error while checking status of ping task.", e);
				String correlationKey = variables.getTarget().getCorrelationKey();
				ErrorListUtils.add(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null),
						variables, correlationKey);
				throw new ErrorBoundaryEvent(ConstantsPing.BPMN_ERROR_CODE_UNEXPECTED_ERROR, ConstantsPing.BPMN_ERROR_MESSAGE_UNEXPECTED_ERROR);
			}
		};
	}
}
