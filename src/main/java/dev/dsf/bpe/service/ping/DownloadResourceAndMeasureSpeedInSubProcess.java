package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.BinaryResourceDownloader;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class DownloadResourceAndMeasureSpeedInSubProcess extends AbstractServiceDelegate
{
	private final long maxDownloadSizeBytes;

	public DownloadResourceAndMeasureSpeedInSubProcess(ProcessPluginApi api, long maxDownloadSizeBytes)
	{
		super(api);
		this.maxDownloadSizeBytes = maxDownloadSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		PingPongLogger logger = new PingPongLogger(DownloadResourceAndMeasureSpeedInSubProcess.class,
				variables.getStartTask());
		logger.debug("Starting resource download to measure speed...");

		Task task = variables.getLatestTask();
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();


		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader(logger,
				CodeSystem.DsfPingProcesses.Code.PING).download(variables, api, task, maxDownloadSizeBytes);

		if (downloadResult.getError() == null)
		{
			variables.setLong(ExecutionVariables.DOWNLOADED_BYTES.correlatedValue(correlationKey),
					downloadResult.getDownloadedBytes());
			variables.setLong(ExecutionVariables.DOWNLOADED_DURATION_MILLIS.correlatedValue(correlationKey),
					downloadResult.getDownloadedDurationMillis());
		}
		else
		{
			delegateExecution.setVariableLocal(ExecutionVariables.RESOURCE_DOWNLOAD_ERROR.getValue(),
					downloadResult.getError());
		}

		logger.debug("Completed resource download and measured speed.");
	}
}
