package dev.dsf.bpe.service;

import java.io.ByteArrayInputStream;
import java.util.Random;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.IdType;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.Process;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.WebApplicationException;

public class GenerateAndStoreResource
{
	private final ProcessPluginApi api;
	private final int maxUploadSizeBytes;
	private final Process process;

	public GenerateAndStoreResource(ProcessPluginApi api, int maxUploadSizeBytes, Process process)
	{
		this.api = api;
		this.maxUploadSizeBytes = maxUploadSizeBytes;
		this.process = process;
	}

	public void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(GenerateAndStoreResource.class, variables.getStartTask());
		logger.debug("Generating resource...");
		int downloadResourceSizeBytes = getDownloadResourceSize(variables);

		byte[] resourceContent = generateRandomBinaryContent(downloadResourceSizeBytes, logger);
		variables.setInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES,
				resourceContent.length);
		logger.debug("Generated resource.");
		logger.debug("Storing binary resource for download...");

		try
		{
			IdType downloadResource = storeBinary(resourceContent, delegateExecution);

			String reference = downloadResource.getValueAsString();

			variables.setString(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_REFERENCE, reference);

			logger.debug("Stored binary resource for download");
		}
		catch (WebApplicationException e)
		{
			String status = String.valueOf(e.getResponse().getStatus());
			ProcessError error = new ProcessError(process.toString(),
					ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_GENERATE_AND_STORE_RESOURCE,
					"Storing Binary resource on local DSF FHIR server.", ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP,
					"Local DSF FHIR server responded with status: " + status);
			variables.setString(ConstantsPing.BPMN_EXECUTION_VARIABLE_RESOURCE_UPLOAD_ERROR,
					ProcessError.toString(error));
		}
	}

	private byte[] generateRandomBinaryContent(int desiredSizeBytes, PingPongLogger logger)
	{
		int sizeBytes = Math.min(maxUploadSizeBytes, desiredSizeBytes);
		byte[] bytes = generateRandomByteArray(sizeBytes);
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

	private int getDownloadResourceSize(Variables variables)
	{
		return variables.getInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES);
	}

	private IdType storeBinary(byte[] downloadResourceContent, DelegateExecution delegateExecution)
	{
		return api.getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().createBinary(
				new ByteArrayInputStream(downloadResourceContent), ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE,
				api.getOrganizationProvider().getLocalOrganization().get().getIdElement().getValue());
	}

}
