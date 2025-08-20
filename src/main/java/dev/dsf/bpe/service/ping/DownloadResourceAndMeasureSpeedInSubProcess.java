package dev.dsf.bpe.service.ping;

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
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.duration.DurationValueImpl;

public class DownloadResourceAndMeasureSpeedInSubProcess extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResourceAndMeasureSpeedInSubProcess.class);

	public DownloadResourceAndMeasureSpeedInSubProcess(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		logger.debug("Starting resource download to measure speed...");

		Task task = variables.getLatestTask();
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();


		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader(
				CodeSystem.DsfPingProcesses.Code.PING).download(variables, api, task);

		if (downloadResult.getError() == null)
		{
			variables.setLong(ExecutionVariables.DOWNLOADED_BYTES.correlatedValue(correlationKey),
					downloadResult.getDownloadedBytes());
			variables.setVariable(ExecutionVariables.DOWNLOADED_DURATION.correlatedValue(correlationKey),
					new DurationValueImpl(downloadResult.getDownloadedDuration()));
		}
		else
		{
			delegateExecution.setVariableLocal(ExecutionVariables.RESOURCE_DOWNLOAD_ERROR.getValue(),
					downloadResult.getError());
		}

		logger.debug("Completed resource download and measured speed.");
	}
}
