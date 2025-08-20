package dev.dsf.bpe.service;

import java.net.SocketTimeoutException;
import java.util.Objects;

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

		CodeSystem.DsfPingProcesses.Code process = getProcess((String) this.process.getValue(delegateExecution));
		Objects.requireNonNull(process);

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

	public void setProcess(org.camunda.bpm.engine.delegate.Expression process)
	{
		this.process = process;
	}

	private CodeSystem.DsfPingProcesses.Code getProcess(String process)
	{
		if (process == null || process.isEmpty())
			return null;
		return CodeSystem.DsfPingProcesses.Code.ofValue(process);
	}
}
