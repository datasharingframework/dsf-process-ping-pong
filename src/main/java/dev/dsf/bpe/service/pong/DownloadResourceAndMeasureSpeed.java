package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.BinaryResourceDownloader;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.duration.DurationValueImpl;

public class DownloadResourceAndMeasureSpeed extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResourceAndMeasureSpeed.class);

	public DownloadResourceAndMeasureSpeed(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		logger.debug("Starting resource download to measure speed...");

		Task task = variables.getStartTask();

		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader(
				CodeSystem.DsfPingProcesses.Code.PONG).download(variables, api, task);

		if (downloadResult.getError() == null)
		{
			variables.setLong(ExecutionVariables.downloadedBytes.name(), downloadResult.getDownloadedBytes());
			variables.setVariable(ExecutionVariables.downloadedDuration.name(),
					new DurationValueImpl(downloadResult.getDownloadedDuration()));
		}
		else
		{
			delegateExecution.setVariable(ExecutionVariables.resourceDownloadError.name(), downloadResult.getError());
		}
		logger.debug("Completed resource download and measured speed.");
	}
}
