package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.service.pong.LogPing;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class Cleanup extends AbstractServiceDelegate
{
	public Cleanup(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(Cleanup.class, variables.getStartTask());
		logger.debug("Cleaning up...");
		String downloadResourceId = new IdType(
				variables.getString(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_REFERENCE)).getIdPart();
		if (downloadResourceId != null)
		{
			api.getFhirWebserviceClientProvider().getLocalWebserviceClient().delete(Binary.class, downloadResourceId);
			api.getFhirWebserviceClientProvider().getLocalWebserviceClient().deletePermanently(Binary.class,
					downloadResourceId);
			logger.debug("Deleted Binary resource with ID {}", downloadResourceId);
		}
		else
		{
			logger.debug("Nothing to do");
		}
		logger.debug("Cleanup complete.");
	}
}
