package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.BinaryResourceDownloader;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.DurationValueImpl;

public class DownloadResourceAndMeasureSpeed extends AbstractServiceDelegate
{
	private final long maxDownloadSizeBytes;

	public DownloadResourceAndMeasureSpeed(ProcessPluginApi api, long maxDownloadSizeBytes)
	{
		super(api);
		this.maxDownloadSizeBytes = maxDownloadSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		PingPongLogger logger = new PingPongLogger(DownloadResourceAndMeasureSpeed.class, variables.getStartTask());
		logger.debug("Starting resource download to measure speed...");

		Task task = variables.getStartTask();

		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader(logger,
				CodeSystem.DsfPingProcesses.Code.PONG).download(variables, api, task, maxDownloadSizeBytes);

		if (downloadResult.getError() == null)
		{
			variables.setLong(ExecutionVariables.DOWNLOADED_BYTES.getValue(), downloadResult.getDownloadedBytes());
			variables.setVariable(ExecutionVariables.DOWNLOADED_DURATION.getValue(),
					new DurationValueImpl(downloadResult.getDownloadedDuration()));
		}
		else
		{
			delegateExecution.setVariable(ExecutionVariables.RESOURCE_DOWNLOAD_ERROR.getValue(),
					downloadResult.getError());
		}
		logger.debug("Completed resource download and measured speed.");
	}
}
