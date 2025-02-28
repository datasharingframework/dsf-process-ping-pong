package dev.dsf.bpe.util;

import java.io.IOException;
import java.io.InputStream;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;

public class BinaryResourceDownloader
{
	private static final Logger logger = LoggerFactory.getLogger(BinaryResourceDownloader.class);

	public DownloadResult download(String webserviceUrl, Variables variables, ProcessPluginApi api, Task task,
			int maxDownloadSizeBytes)
	{
		DownloadResult downloadResult;

		int downloadResourceSizeBytes = variables
				.getInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES);

		Reference downloadResourceReference = api.getTaskHelper()
				.getFirstInputParameterValue(task, ConstantsPing.CODESYSTEM_DSF_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOAD_RESOURCE_REFERENCE, Reference.class)
				.orElseThrow();

		InputStream binaryResourceInputStream = api.getFhirWebserviceClientProvider().getWebserviceClient(webserviceUrl)
				.readBinary(getDownloadResourceId(downloadResourceReference),
						ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE);

		try (binaryResourceInputStream)
		{
			logger.info(
					"Starting resource download for: '{}'. Requested resource size is {} bytes, maximum downloadable size is {} bytes",
					webserviceUrl + "/" + getDownloadResourceId(downloadResourceReference), downloadResourceSizeBytes,
					maxDownloadSizeBytes);
			long downloadStartTime = System.currentTimeMillis();
			int bufferSize = Math.min(downloadResourceSizeBytes, maxDownloadSizeBytes);
			byte[] buffer = new byte[bufferSize];
			int bytesRead = binaryResourceInputStream.read(buffer);
			long downloadEndTime = System.currentTimeMillis();
			long downloadedDurationMillis = downloadEndTime - downloadStartTime;
			downloadResult = new DownloadResult(bytesRead, downloadedDurationMillis);
			logger.info("Finished downloading {} bytes. Took {}", bytesRead,
					toHoursMinutesSecondsMilliseconds(downloadedDurationMillis));

		}
		catch (IOException e)
		{
			downloadResult = new DownloadResult(e.getMessage());
		}
		return downloadResult;
	}

	private String getDownloadResourceId(Reference downloadResourceReference)
	{
		String[] split = downloadResourceReference.getReference().split("/");
		return split[1];
	}

	private String toHoursMinutesSecondsMilliseconds(long millis)
	{
		long hours = (millis / 1000) / 60 / 60 % 24;
		long minutes = (millis / 1000) / 60 % 60;
		long seconds = (millis / 1000) % 60;
		long milliSeconds = millis % 1000;
		return String.format("%02d:%02d:%02d:%02dms", hours, minutes, seconds, milliSeconds);
	}

	public static class DownloadResult
	{
		private final int downloadedBytes;
		private final long downloadedDurationMillis;
		private final String errorMessage;

		public DownloadResult(int downloadedBytes, long downloadedDurationMillis)
		{
			this.downloadedBytes = downloadedBytes;
			this.downloadedDurationMillis = downloadedDurationMillis;
			errorMessage = null;
		}

		public DownloadResult(String errorMessage)
		{
			downloadedBytes = 0;
			downloadedDurationMillis = 0;
			this.errorMessage = errorMessage;
		}

		public int getDownloadedBytes()
		{
			return downloadedBytes;
		}

		public long getDownloadedDurationMillis()
		{
			return downloadedDurationMillis;
		}

		public String getErrorMessage()
		{
			return errorMessage;
		}
	}
}
