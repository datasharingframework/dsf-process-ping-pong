package dev.dsf.bpe.service;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.DecimalType;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class SetDownloadResourceSize extends AbstractServiceDelegate
{
	private final long maxDownloadResourceSizeBytes;

	public SetDownloadResourceSize(ProcessPluginApi api, long maxDownloadResourceSizeBytes)
	{
		super(api);
		this.maxDownloadResourceSizeBytes = maxDownloadResourceSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(SetDownloadResourceSize.class, variables.getStartTask());
		logger.debug("Setting download resource size...");

		variables.setLong(ExecutionVariables.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue(),
				getDownloadResourceSize(variables));

		logger.debug("Set download resource size to " + maxDownloadResourceSizeBytes);
	}

	private long getDownloadResourceSize(Variables variables)
	{
		Optional<DecimalType> downloadResourceSizeType = api.getTaskHelper().getFirstInputParameterValue(
				variables.getStartTask(), ConstantsPing.CODESYSTEM_DSF_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOAD_RESOURCE_SIZE_BYTES, DecimalType.class);

		return downloadResourceSizeType.map(decimalType -> decimalType.getValue().longValue()).orElseGet(
				() -> Math.min(maxDownloadResourceSizeBytes, ConstantsPing.DOWNLOAD_RESOURCE_SIZE_BYTES_DEFAULT));
	}
}
