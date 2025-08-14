package dev.dsf.bpe.service;

import java.net.SocketTimeoutException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.service.pong.CleanupPong;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

public class Cleanup
{
	private final ProcessPluginApi api;
	private final CodeSystem.DsfPingProcesses.Code process;

	public Cleanup(ProcessPluginApi api, CodeSystem.DsfPingProcesses.Code process)
	{
		this.api = api;
		this.process = process;
	}

	public void doExecute(DelegateExecution delegateExecution, Variables variables)
	{
		PingPongLogger logger = new PingPongLogger(CleanupPong.class, variables.getStartTask());
		logger.debug("Cleaning up...");
		String downloadResourceId = new IdType(
				variables.getString(ExecutionVariables.DOWNLOAD_RESOURCE_REFERENCE.getValue())).getIdPart();
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
				if (e.getCause() instanceof SocketTimeoutException)
				{
					ProcessError error = new ProcessError(process, CodeSystem.DsfPingProcessSteps.Code.CLEANUP,
							ConstantsPing.CLEANUP_ERROR_ACTION, ConstantsPing.POTENTIAL_FIX_URL_READ_TIMEOUT,
							e.getCause().getMessage());
					ErrorListUtils.add(error, delegateExecution);
				}
				else
				{
					throw new RuntimeException(e);
				}
			}
			catch (WebApplicationException e)
			{
				ProcessError error = new ProcessError(process, CodeSystem.DsfPingProcessSteps.Code.CLEANUP,
						ConstantsPing.CLEANUP_ERROR_ACTION, ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP,
						"Response from local DSF FHIR server: " + e.getResponse().getStatus());
				ErrorListUtils.add(error, delegateExecution);
			}
		}
		else
		{
			logger.debug("Nothing to do");
		}
		logger.debug("Cleanup complete.");
	}
}
