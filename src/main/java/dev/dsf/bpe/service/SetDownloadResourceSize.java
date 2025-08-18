package dev.dsf.bpe.service;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class SetDownloadResourceSize extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(SetDownloadResourceSize.class);
	private final long maxDownloadResourceSizeBytes;

	public SetDownloadResourceSize(ProcessPluginApi api, long maxDownloadResourceSizeBytes)
	{
		super(api);
		this.maxDownloadResourceSizeBytes = maxDownloadResourceSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		logger.debug("Setting download resource size...");

		variables.setLong(ExecutionVariables.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue(),
				getDownloadResourceSize(variables));

		logger.debug("Set download resource size to " + maxDownloadResourceSizeBytes);
	}

	private long getDownloadResourceSize(Variables variables)
	{
		Optional<DecimalType> downloadResourceSizeType = api.getTaskHelper().getFirstInputParameterValue(
				variables.getStartTask(), CodeSystem.DsfPing.URL,
				CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue(), DecimalType.class);

		return downloadResourceSizeType.map(decimalType -> decimalType.getValue().longValue()).orElseGet(
				() -> Math.min(maxDownloadResourceSizeBytes, ConstantsPing.DOWNLOAD_RESOURCE_SIZE_BYTES_DEFAULT));
	}
}
