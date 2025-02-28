package dev.dsf.bpe.service;

import static dev.dsf.bpe.ConstantsPing.BPMN_EXECUTION_VARIABLE_ERROR_MESSAGE_LIST;
import static dev.dsf.bpe.ConstantsPing.WEBSERVICE_URL_PATTERN;

import java.io.InputStream;
import java.util.regex.Matcher;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Reference;
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

		int downloadResourceSizeBytes = variables
				.getInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES);

		Reference downloadResourceReference = api.getTaskHelper()
				.getFirstInputParameterValue(task, ConstantsPing.CODESYSTEM_DSF_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOAD_RESOURCE_REFERENCE, Reference.class)
				.orElseThrow();

		Matcher matcher = WEBSERVICE_URL_PATTERN.matcher(downloadResourceReference.getReference());
		boolean matched = matcher.find();
		if (matched)
		{
			String webserviceUrl = matcher.group(1);

			InputStream binaryResourceInputStream = api.getFhirWebserviceClientProvider()
					.getWebserviceClient(webserviceUrl).readBinary(getDownloadResourceId(downloadResourceReference),
							ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE);

			BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader().download(
					binaryResourceInputStream, downloadResourceReference.getReference(), downloadResourceSizeBytes,
					maxDownloadSizeBytes);

			if (downloadResult.getErrorMessage() != null)
			{
				variables.setInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOADED_BYTES,
						downloadResult.getDownloadedBytes());
				variables.setLong(
						ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOADED_DURATION_MILLIS + "_" + correlationKey,
						downloadResult.getDownloadedDurationMillis());
			}
			else
			{
				ErrorMessageListUtils.add(downloadResult.getErrorMessage(),
						BPMN_EXECUTION_VARIABLE_ERROR_MESSAGE_LIST + "_" + correlationKey, delegateExecution);
			}
		}
		else
		{
			ErrorMessageListUtils.add("Invalid download resource url:" + downloadResourceReference.getReference(),
					BPMN_EXECUTION_VARIABLE_ERROR_MESSAGE_LIST + "_" + correlationKey, delegateExecution);
		}
	}

	private String getDownloadResourceId(Reference downloadResourceReference)
	{
		String[] split = downloadResourceReference.getReference().split("/");
		return split[split.length - 1];
	}
}
