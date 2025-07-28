package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
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
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(DownloadResourceAndMeasureSpeedInSubProcess.class,
				variables.getStartTask());
		logger.debug("Starting resource download to measure speed...");

		Task task = variables.getLatestTask();
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();


		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader(logger,
				ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PING)
				.download(variables, api, task, maxDownloadSizeBytes);

		if (downloadResult.getError() == null)
		{
			variables.setLong(ConstantsPing.getBpmnExecutionVariableDownloadedBytes(correlationKey),
					downloadResult.getDownloadedBytes());
			variables.setLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(correlationKey),
					downloadResult.getDownloadedDurationMillis());
		}
		else
		{
			delegateExecution.setVariableLocal(ConstantsPing.BPMN_EXECUTION_VARIABLE_RESOURCE_DOWNLOAD_ERROR,
					downloadResult.getError());
		}

		logger.debug("Completed resource download and measured speed.");
	}
}
