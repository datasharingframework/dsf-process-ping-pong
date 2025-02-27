package dev.dsf.bpe.service;

import java.util.Base64;
import java.util.Random;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.ReadAccessTagGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class GenerateResource extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(GenerateResource.class);
	private final int maxUploadSizeBytes;

	public GenerateResource(ProcessPluginApi api, int maxUploadSizeBytes)
	{
		super(api);
		this.maxUploadSizeBytes = maxUploadSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		int downloadResourceSizeBytes = getDownloadResourceSize(variables);

		variables.setByteArray(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE,
				generateRandomBinaryContent(downloadResourceSizeBytes));

		logger.info("Generated binary resource content for network speed measurement");
	}

	private byte[] generateRandomBinaryContent(int desiredSizeBytes)
	{
		int sizeBytes = Math.min(maxUploadSizeBytes, desiredSizeBytes);
		byte[] bytes = base64Encode(generateRandomByteArray((sizeBytes / 4) * 3));
		logger.info(
				"Generated binary content for network speed measurement. Requested size was: {} bytes, generated size was : {}",
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
}
