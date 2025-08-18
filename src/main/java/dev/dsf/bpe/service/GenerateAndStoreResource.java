package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.process_error.ProcessErrorValueImpl;
import jakarta.ws.rs.WebApplicationException;

public class GenerateAndStoreResource
{
	private static final Logger logger = LoggerFactory.getLogger(GenerateAndStoreResource.class);
	private final ProcessPluginApi api;
	private final long maxUploadSizeBytes;
	private final CodeSystem.DsfPingProcesses.Code process;

	public GenerateAndStoreResource(ProcessPluginApi api, long maxUploadSizeBytes,
			CodeSystem.DsfPingProcesses.Code process)
	{
		this.api = api;
		this.maxUploadSizeBytes = maxUploadSizeBytes;
		this.process = process;
	}

	public void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		logger.debug("Generating resource...");
		long downloadResourceSizeBytes = getDownloadResourceSize(variables);
		RandomByteInputStream resourceContent;
		if (downloadResourceSizeBytes > maxUploadSizeBytes)
		{
			logger.info(
					"Requested resource size of {} bytes exceeds configured maximum upload size of {} bytes. Trimmed to maximum upload size.",
					downloadResourceSizeBytes, maxUploadSizeBytes);
			resourceContent = new RandomByteInputStream(maxUploadSizeBytes);
		}
		else
		{
			resourceContent = new RandomByteInputStream(downloadResourceSizeBytes);
		}
		variables.setLong(ExecutionVariables.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue(), downloadResourceSizeBytes);
		logger.debug("Generated resource.");
		logger.debug("Storing binary resource for download...");

		try
		{
			IdType downloadResource = storeBinary(resourceContent);

			String reference = downloadResource.toVersionless().getValueAsString();

			variables.setString(ExecutionVariables.DOWNLOAD_RESOURCE_REFERENCE.getValue(), reference);

			logger.debug("Stored binary resource for download");
		}
		catch (WebApplicationException e)
		{
			String status = String.valueOf(e.getResponse().getStatus());
			ProcessError error = new ProcessError(process,
					CodeSystem.DsfPingProcessSteps.Code.GENERATE_AND_STORE_RESOURCE,
					"Storing Binary resource on local DSF FHIR server.", ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP,
					"Local DSF FHIR server responded with status: " + status);
			variables.setVariable(ExecutionVariables.RESOURCE_UPLOAD_ERROR.getValue(),
					new ProcessErrorValueImpl(error));
		}
	}

	private long getDownloadResourceSize(Variables variables)
	{
		return variables.getLong(ExecutionVariables.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue());
	}

	private IdType storeBinary(RandomByteInputStream downloadResourceContent)
	{
		return api.getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().createBinary(
				downloadResourceContent, ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE,
				api.getOrganizationProvider().getLocalOrganization().get().getIdElement().getValue());
	}

}
