package dev.dsf.bpe.service;

import static dev.dsf.bpe.ConstantsPing.BPMN_EXECUTION_VARIABLE_ERROR_MESSAGE_LIST;

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
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class DownloadResourceAndMeasureSpeedInSubProcess extends AbstractServiceDelegate
{
	public static final Logger logger = LoggerFactory.getLogger(DownloadResourceAndMeasureSpeedInSubProcess.class);
	private final int maxDownloadSizeBytes;

	public DownloadResourceAndMeasureSpeedInSubProcess(ProcessPluginApi api, int maxDownloadSizeBytes)
	{
		super(api);
		this.maxDownloadSizeBytes = maxDownloadSizeBytes;
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		Task task = variables.getLatestTask();
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();

		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader().download(variables, api,
				task, maxDownloadSizeBytes);

		if (downloadResult.getErrorMessage() == null)
		{
			variables.setInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOADED_BYTES,
					downloadResult.getDownloadedBytes());
			variables.setLong(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOADED_DURATION_MILLIS + "_" + correlationKey,
					downloadResult.getDownloadedDurationMillis());
		}
		else
		{
			ErrorMessageListUtils.add(downloadResult.getErrorMessage(),
					BPMN_EXECUTION_VARIABLE_ERROR_MESSAGE_LIST + "_" + correlationKey, delegateExecution);
		}
	}
}
