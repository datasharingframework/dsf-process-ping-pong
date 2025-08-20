package dev.dsf.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.process_error.ProcessErrorValueImpl;
import jakarta.ws.rs.WebApplicationException;

public class GenerateAndStoreResource extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(GenerateAndStoreResource.class);
	private final long maxUploadSizeBytes;
	private Expression process;

	public GenerateAndStoreResource(ProcessPluginApi api, long maxUploadSizeBytes)
	{
		super(api);
		this.maxUploadSizeBytes = maxUploadSizeBytes;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
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
		variables.setLong(ExecutionVariables.downloadResourceSizeBytes.name(), downloadResourceSizeBytes);
		logger.debug("Generated resource.");
		logger.debug("Storing binary resource for download...");

		try
		{
			IdType downloadResource = storeBinary(resourceContent);

			String reference = downloadResource.toVersionless().getValueAsString();

			variables.setString(ExecutionVariables.downloadResourceReference.name(), reference);

			logger.debug("Stored binary resource for download");
		}
		catch (WebApplicationException e)
		{
			String status = String.valueOf(e.getResponse().getStatus());
			ProcessError error = new ProcessError(getProcess((String) process.getValue(delegateExecution)),
					CodeSystem.DsfPingProcessSteps.Code.GENERATE_AND_STORE_RESOURCE,
					"Storing Binary resource on local DSF FHIR server.", ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP,
					"Local DSF FHIR server responded with status: " + status);
			variables.setVariable(ExecutionVariables.resourceUploadError.name(), new ProcessErrorValueImpl(error));
		}
	}

	private long getDownloadResourceSize(Variables variables)
	{
		return variables.getLong(ExecutionVariables.downloadResourceSizeBytes.name());
	}

	private IdType storeBinary(RandomByteInputStream downloadResourceContent)
	{
		return api.getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().createBinary(
				downloadResourceContent, ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE,
				api.getOrganizationProvider().getLocalOrganization().get().getIdElement().getValue());
	}

	private CodeSystem.DsfPingProcesses.Code getProcess(String process)
	{
		if (process == null || process.isEmpty())
			return null;
		return CodeSystem.DsfPingProcesses.Code.ofValue(process);
	}
}
