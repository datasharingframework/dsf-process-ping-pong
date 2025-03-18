package dev.dsf.bpe.service;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.IntegerType;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class SetDownloadResourceSize extends AbstractServiceDelegate
{
	private final int maxDownloadResourceSizeBytes;

	public SetDownloadResourceSize(ProcessPluginApi api, int maxDownloadResourceSizeBytes)
	{
		super(api);
		this.maxDownloadResourceSizeBytes = maxDownloadResourceSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(SetDownloadResourceSize.class, variables.getStartTask());
		logger.debug("Setting download resource size...");

		variables.setInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES,
				getDownloadResourceSize(variables));

		logger.debug("Set download resource size to " + maxDownloadResourceSizeBytes);
	}

	private int getDownloadResourceSize(Variables variables)
	{
		Optional<IntegerType> downloadResourceSizeType = api.getTaskHelper().getFirstInputParameterValue(
				variables.getStartTask(), ConstantsPing.CODESYSTEM_DSF_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOAD_RESOURCE_SIZE_BYTES, IntegerType.class);

		return downloadResourceSizeType.isPresent() ? downloadResourceSizeType.get().getValue()
				: Math.min(maxDownloadResourceSizeBytes, ConstantsPing.DOWNLOAD_RESOURCE_SIZE_BYTES_DEFAULT);
	}
}
