package dev.dsf.bpe.service;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.ReadAccessTagGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreResource extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(StoreResource.class);

	public StoreResource(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		String localEndpointAddress = api.getEndpointProvider().getLocalEndpoint().orElseThrow().getAddress();

		Binary downloadResource = storeBinary(
				variables.getByteArray(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE));

		String reference = localEndpointAddress + "/Binary/" + downloadResource.getId();

		variables.setString(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_REFERENCE, reference);

		logger.info("Stored binary resource for PING targets to download");
	}

	private Binary storeBinary(byte[] downloadResourceContent)
	{
		Binary downloadResource = new Binary();
		downloadResource.setContent(downloadResourceContent);
		downloadResource.setContentType(ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE.toString());
		downloadResource.setId(UUID.randomUUID().toString());
		downloadResource.getMeta()
				.addTag(ReadAccessTagGenerator.create(ConstantsPing.CODESYSTEM_READ_ACCESS_TAG_VALUE_ALL));

		return api.getFhirWebserviceClientProvider().getLocalWebserviceClient().create(downloadResource);
	}
}
