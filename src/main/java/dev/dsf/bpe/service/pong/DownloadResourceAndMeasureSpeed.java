package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.BinaryResourceDownloader;
import dev.dsf.bpe.util.ErrorMessageListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class DownloadResourceAndMeasureSpeed extends AbstractServiceDelegate
{
	private final int maxDownloadSizeBytes;

	public DownloadResourceAndMeasureSpeed(ProcessPluginApi api, int maxDownloadSizeBytes)
	{
		super(api);
		this.maxDownloadSizeBytes = maxDownloadSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(DownloadResourceAndMeasureSpeed.class, variables.getStartTask());
		logger.debug("Starting resource download to measure speed...");

		Task task = variables.getStartTask();

		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader(logger)
				.download(variables, api, task, maxDownloadSizeBytes);

		if (downloadResult.getErrorMessage() == null)
		{
			variables.setInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes(),
					downloadResult.getDownloadedBytes());
			variables.setLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(),
					downloadResult.getDownloadedDurationMillis());
		}
		else
		{
			throw new BpmnError(ConstantsPing.BPMN_ERROR_CODE_RESOURCE_DOWNLOAD_ERROR, downloadResult.getErrorMessage());
		}

		logger.debug("Completed resource download and measured speed.");
	}
}
