package dev.dsf.bpe.service;

import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.IdType;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.ReadAccessTagGenerator;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreResource extends AbstractServiceDelegate
{
	public StoreResource(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(StoreResource.class, variables.getStartTask());
		logger.debug("Storing binary resource for download...");

		IdType downloadResource = storeBinary(
				variables.getByteArray(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE));

		String reference = downloadResource.getValueAsString();

		variables.setString(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_REFERENCE, reference);

		logger.debug("Stored binary resource for download");
	}

	private IdType storeBinary(byte[] downloadResourceContent)
	{
		Binary downloadResource = new Binary();
		downloadResource.setContent(downloadResourceContent);
		downloadResource.setContentType(ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE.toString());
		downloadResource.setId(UUID.randomUUID().toString());
		downloadResource.getMeta()
				.addTag(ReadAccessTagGenerator.create(ConstantsPing.CODESYSTEM_READ_ACCESS_TAG_VALUE_ALL));

		return api.getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn()
				.create(downloadResource);
	}
}
