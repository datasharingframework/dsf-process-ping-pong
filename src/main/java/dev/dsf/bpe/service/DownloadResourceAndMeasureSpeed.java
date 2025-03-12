package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.BinaryResourceDownloader;
import dev.dsf.bpe.util.ErrorMessageListUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class DownloadResourceAndMeasureSpeed extends AbstractServiceDelegate
{
	public static final Logger logger = LoggerFactory.getLogger(DownloadResourceAndMeasureSpeed.class);
	private final int maxDownloadSizeBytes;

	public DownloadResourceAndMeasureSpeed(ProcessPluginApi api, int maxDownloadSizeBytes)
	{
		super(api);
		this.maxDownloadSizeBytes = maxDownloadSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		Task task = variables.getStartTask();

		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader().download(variables, api,
				task, maxDownloadSizeBytes);

		if (downloadResult.getErrorMessage() == null)
		{
			variables.setInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes(),
					downloadResult.getDownloadedBytes());
			variables.setLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(),
					downloadResult.getDownloadedDurationMillis());
		}
		else
		{
			ErrorMessageListUtils.add(downloadResult.getErrorMessage(), delegateExecution);
		}
	}
}
