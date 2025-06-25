package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.BinaryResourceDownloader;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
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

		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader(logger,
				ConstantsPing.CODESYSTEM_DSF_PING_PROCESSES_VALUE_PONG)
				.download(variables, api, task, maxDownloadSizeBytes);

		if (downloadResult.getError() == null)
		{
			variables.setInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes(),
					downloadResult.getDownloadedBytes());
			variables.setLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(),
					downloadResult.getDownloadedDurationMillis());
		}
		else
		{
			delegateExecution.setVariable(ConstantsPing.BPMN_EXECUTION_VARIABLE_RESOURCE_DOWNLOAD_ERROR,
					downloadResult.getError());
			throw new BpmnError(ConstantsPing.BPMN_ERROR_CODE_RESOURCE_DOWNLOAD_ERROR);
		}
		logger.debug("Completed resource download and measured speed.");
	}
}
