package dev.dsf.bpe.service;

import static dev.dsf.bpe.ConstantsPing.BPMN_EXECUTION_VARIABLE_ERROR_MESSAGE_LIST;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
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
		String endpointIdentifier = variables
				.getString(ConstantsPing.BPMN_EXECUTION_VARIABLE_PONG_TARGET_ENDPOINT_IDENTIFIER);
		String webserviceUrl = api.getEndpointProvider().getEndpointAddress(endpointIdentifier).orElseThrow();

		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader().download(webserviceUrl,
				variables, api, task, maxDownloadSizeBytes);

		if (downloadResult.getErrorMessage() != null)
		{
			variables.setInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOADED_BYTES,
					downloadResult.getDownloadedBytes());
			variables.setLong(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOADED_DURATION_MILLIS,
					downloadResult.getDownloadedDurationMillis());
		}
		else
		{
			ErrorMessageListUtils.add(downloadResult.getErrorMessage(), BPMN_EXECUTION_VARIABLE_ERROR_MESSAGE_LIST,
					delegateExecution);
		}
	}
}
