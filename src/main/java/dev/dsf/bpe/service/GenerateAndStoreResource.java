package dev.dsf.bpe.service;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Random;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.IdType;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class GenerateAndStoreResource extends AbstractServiceDelegate
{
	private final int maxUploadSizeBytes;

	public GenerateAndStoreResource(ProcessPluginApi api, int maxUploadSizeBytes)
	{
		super(api);
		this.maxUploadSizeBytes = maxUploadSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(GenerateAndStoreResource.class, variables.getStartTask());
		logger.debug("Generating resource...");
		int downloadResourceSizeBytes = getDownloadResourceSize(variables);

		variables.setByteArray(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE,
				generateRandomBinaryContent(downloadResourceSizeBytes, logger));
		logger.debug("Generated resource.");
		logger.debug("Storing binary resource for download...");

		IdType downloadResource = storeBinary(
				variables.getByteArray(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE));

		String reference = downloadResource.getValueAsString();

		variables.setString(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_REFERENCE, reference);

		logger.debug("Stored binary resource for download");
	}

	private byte[] generateRandomBinaryContent(int desiredSizeBytes, PingPongLogger logger)
	{
		int sizeBytes = Math.min(maxUploadSizeBytes, desiredSizeBytes);
		byte[] bytes = base64Encode(generateRandomByteArray((sizeBytes / 4) * 3));
		logger.info(
				"Generated binary content for network speed measurement. Requested size was: {} bytes, generated size (base64 encoded) was : {}",
				desiredSizeBytes, bytes.length);
		return bytes;
	}

	private byte[] generateRandomByteArray(int sizeBytes)
	{
		Random rand = new Random();
		byte[] randomBytes = new byte[sizeBytes];
		rand.nextBytes(randomBytes);
		return randomBytes;
	}

	private byte[] base64Encode(byte[] content)
	{
		return Base64.getEncoder().encode(content);
	}

	private int getDownloadResourceSize(Variables variables)
	{
		return variables.getInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES);
	}

	private IdType storeBinary(byte[] downloadResourceContent)
	{
		return api.getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn()
				.createBinary(new ByteArrayInputStream(downloadResourceContent), ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE, api.getOrganizationProvider().getLocalOrganization().get().getIdElement().getIdPart());
	}
}
