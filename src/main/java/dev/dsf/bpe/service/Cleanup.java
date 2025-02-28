package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class Cleanup extends AbstractServiceDelegate
{
	private final static Logger logger = LoggerFactory.getLogger(Cleanup.class);

	public Cleanup(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		logger.info("Cleaning up...");
		String downloadResourceId = variables.getString(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_ID);
		if (downloadResourceId != null)
		{
			api.getFhirWebserviceClientProvider().getLocalWebserviceClient().delete(Binary.class, downloadResourceId);
			api.getFhirWebserviceClientProvider().getLocalWebserviceClient().deletePermanently(Binary.class,
					downloadResourceId);
			logger.info("Deleted Binary resource with ID {}", downloadResourceId);
		}
		else
		{
			logger.info("Nothing to do");
		}
		logger.info("Cleanup complete.");
	}
}
