package dev.dsf.bpe.service;

import java.net.SocketTimeoutException;
import java.util.Locale;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

public class Cleanup extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(Cleanup.class);
	private Expression process;

	public Cleanup(ProcessPluginApi api)
	{
		super(api);
	}

	public void doExecute(DelegateExecution delegateExecution, Variables variables)
	{
		logger.debug("Cleaning up...");

		String process = (String) this.process.getValue(delegateExecution);

		String downloadResourceId = new IdType(variables.getString(ExecutionVariables.downloadResourceReference.name()))
				.getIdPart();
		if (downloadResourceId != null)
		{
			try
			{
				api.getFhirWebserviceClientProvider().getLocalWebserviceClient().delete(Binary.class,
						downloadResourceId);
				api.getFhirWebserviceClientProvider().getLocalWebserviceClient().deletePermanently(Binary.class,
						downloadResourceId);
				logger.debug("Deleted Binary resource with ID {}", downloadResourceId);
			}
			catch (ProcessingException e)
			{
				if (e.getCause() instanceof SocketTimeoutException timeoutException)
				{
					ProcessError error = toProcessError(timeoutException, process);
					ErrorListUtils.add(error, delegateExecution);
					logger.error(e.getCause().getMessage());
				}
				else if (e.getCause() instanceof ConnectTimeoutException)
				{
					ProcessError error = toProcessErrorConnectTimeout(process);
					ErrorListUtils.add(error, delegateExecution);
					logger.error(e.getCause().getMessage());
				}
				else if (e.getCause() instanceof HttpHostConnectException)
				{
					ProcessError error = toProcessErrorLocalHttpHostConnect(process);
					ErrorListUtils.add(error, delegateExecution);
					logger.error(e.getCause().getMessage());
				}
				else
				{
					ProcessError error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null);
					ErrorListUtils.add(error, delegateExecution);
					logger.error("Unexpected error: {}", e.getCause().getMessage());
				}
			}
			catch (WebApplicationException e)
			{
				ProcessError error = toProcessError(e, process);
				ErrorListUtils.add(error, delegateExecution);
			}
		}
		else
		{
			logger.debug("Nothing to do");
		}
		logger.debug("Cleanup complete.");
	}

	private ProcessError toProcessError(SocketTimeoutException timeoutException, String process)
	{
		ProcessError error;
		String message = timeoutException.getMessage().toLowerCase(Locale.ROOT);
		if (message.contains("connect"))
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_TIMEOUT_CONNECT,
					ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
		}
		else if (message.contains("read"))
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_TIMEOUT_READ,
					ConstantsPing.POTENTIAL_FIX_URL_READ_TIMEOUT);
		}
		else
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null);
			logger.error("Unexpected error: {}", timeoutException.getMessage());
		}
		return error;
	}

	private ProcessError toProcessErrorLocalHttpHostConnect(String process)
	{
		return new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_HTTP_HOST_CONNECT,
				ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED);
	}

	private ProcessError toProcessErrorConnectTimeout(String process)
	{
		return new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_TIMEOUT_CONNECT,
				ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
	}

	private ProcessError toProcessError(WebApplicationException e, String process)
	{
		int status = e.getResponse().getStatus();
		String message = "Response from local DSF FHIR server: " + status;
		logger.error(message, e);

		return switch (status)
		{
			case 401 -> new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_HTTP_401,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 403 -> new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_HTTP_403,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 407 -> new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_HTTP_407,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 500 -> new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_HTTP_500,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 502 -> new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_HTTP_502,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 503 -> new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_HTTP_503,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			case 504 -> new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_HTTP_504,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
			default -> new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DELETE_HTTP_UNEXPECTED,
					null);
		};
	}

	public void setProcess(Expression process)
	{
		this.process = process;
	}
}
