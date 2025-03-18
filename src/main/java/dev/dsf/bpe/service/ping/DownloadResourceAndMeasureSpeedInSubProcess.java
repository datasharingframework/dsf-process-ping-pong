package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.service.pong.LogPing;
import dev.dsf.bpe.util.BinaryResourceDownloader;
import dev.dsf.bpe.util.ErrorMessageListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class DownloadResourceAndMeasureSpeedInSubProcess extends AbstractServiceDelegate
{
	private final int maxDownloadSizeBytes;

	public DownloadResourceAndMeasureSpeedInSubProcess(ProcessPluginApi api, int maxDownloadSizeBytes)
	{
		super(api);
		this.maxDownloadSizeBytes = maxDownloadSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(DownloadResourceAndMeasureSpeedInSubProcess.class,
				variables.getStartTask());
		logger.debug("Starting resource download to measure speed...");

		Task task = variables.getLatestTask();
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();

		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader(logger)
				.download(variables, api, task, maxDownloadSizeBytes);

		if (downloadResult.getErrorMessage() == null)
		{
			variables.setInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes(correlationKey),
					downloadResult.getDownloadedBytes());
			variables.setLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(correlationKey),
					downloadResult.getDownloadedDurationMillis());
		}
		else
		{
			ErrorMessageListUtils.add(downloadResult.getErrorMessage(), delegateExecution);
		}

		logger.debug("Completed resource download and measured speed.");
	}
}
