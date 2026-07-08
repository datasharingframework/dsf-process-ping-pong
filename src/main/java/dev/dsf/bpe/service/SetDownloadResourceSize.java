package dev.dsf.bpe.service;

import java.util.Optional;

import org.hl7.fhir.r4.model.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class SetDownloadResourceSize implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SetDownloadResourceSize.class);
	private final long maxDownloadResourceSizeBytes;

	public SetDownloadResourceSize(long maxDownloadResourceSizeBytes)
	{
		if (maxDownloadResourceSizeBytes < 0)
		{
			this.maxDownloadResourceSizeBytes = 0L;
		}
		else
		{
			this.maxDownloadResourceSizeBytes = maxDownloadResourceSizeBytes;
		}
	}

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Setting download resource size...");

		long downloadResourceSizeBytes = getDownloadResourceSizeBytes(api, variables);
		variables.setLong(ExecutionVariables.downloadResourceSizeBytes.name(), downloadResourceSizeBytes);

		logger.debug("Set download resource size to " + downloadResourceSizeBytes);

		variables.setLong(ExecutionVariables.maxDownloadResourceSizeBytes.name(), maxDownloadResourceSizeBytes);

		logger.debug("Set maximum download resource size to " + maxDownloadResourceSizeBytes);
	}

	private long getDownloadResourceSizeBytes(ProcessPluginApi api, Variables variables)
	{
		Optional<DecimalType> downloadResourceSizeType = api.getTaskHelper().getFirstInputParameterValue(
				variables.getStartTask(), CodeSystem.DsfPing.URL,
				CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue(), DecimalType.class);

		return downloadResourceSizeType.map(decimalType -> decimalType.getValue().longValue())
				.orElse(Math.min(maxDownloadResourceSizeBytes, ConstantsPing.DOWNLOAD_RESOURCE_SIZE_BYTES_DEFAULT));
	}
}
