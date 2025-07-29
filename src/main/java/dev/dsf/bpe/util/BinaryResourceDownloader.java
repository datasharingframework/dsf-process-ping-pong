package dev.dsf.bpe.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Optional;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

public class BinaryResourceDownloader
{
	private final PingPongLogger logger;
	private final String process;

	public BinaryResourceDownloader(PingPongLogger logger, String process)
	{
		this.logger = logger;
		this.process = process;
	}

	public DownloadResult download(Variables variables, ProcessPluginApi api, Task task, long maxDownloadSizeBytes)
	{
		DownloadResult downloadResult;

		long downloadResourceSizeBytes = variables.getLong(ExecutionVariables.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue());

		Optional<Reference> optDownloadResourceReference = api.getTaskHelper().getFirstInputParameterValue(task,
				ConstantsPing.CODESYSTEM_DSF_PING, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOAD_RESOURCE_REFERENCE,
				Reference.class);

		if (optDownloadResourceReference.isEmpty())
		{
			ProcessError error = new ProcessError(process,
					ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_DOWNLOAD_RESOURCE_AND_MEASURE_SPEED,
					"Extracting binary resource reference from task " + task.getIdElement().getValue(), null,
					"No reference provided in task");
			downloadResult = new DownloadResult(error);
			return downloadResult;
		}

		Reference downloadResourceReference = optDownloadResourceReference.get();
		IdType downloadResourceReferenceIdType = new IdType(downloadResourceReference.getReference());
		String downloadResourceReferenceId = downloadResourceReferenceIdType.getIdPart();
		String webserviceUrl = downloadResourceReferenceIdType.getBaseUrl();
		String action = "Downloading binary resource from " + webserviceUrl;
		try
		{
			InputStream binaryResourceInputStream = api.getFhirWebserviceClientProvider()
					.getWebserviceClient(webserviceUrl)
					.readBinary(downloadResourceReferenceId, ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE);

			try (binaryResourceInputStream)
			{
				logger.info(
						"Downloading resource for: '{}'. Requested resource size is {} bytes, maximum downloadable size is {} bytes...",
						downloadResourceReference.getReference(), downloadResourceSizeBytes, maxDownloadSizeBytes);
				long downloadStartTime = System.currentTimeMillis();
				long numBytes = Math.min(downloadResourceSizeBytes, maxDownloadSizeBytes);
				binaryResourceInputStream.skipNBytes(numBytes);
				long downloadEndTime = System.currentTimeMillis();
				long downloadedDurationMillis = downloadEndTime - downloadStartTime;
				downloadResult = new DownloadResult(numBytes, downloadedDurationMillis);
				logger.info("Finished downloading {} bytes. Took {}", numBytes,
						toHoursMinutesSecondsMilliseconds(downloadedDurationMillis));

			}
			catch (IOException e)
			{
				binaryResourceInputStream.close();
				String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
				ProcessError error = new ProcessError(process,
						ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_DOWNLOAD_RESOURCE_AND_MEASURE_SPEED,
						action, null, errorMessage);
				logger.error("Encountered an error while downloading resource: {}", errorMessage);
				downloadResult = new DownloadResult(error);
			}
		}
		catch (WebApplicationException e)
		{
			String errorMessage = (e.getResponse().getStatusInfo().getStatusCode() + " " + e.getMessage()).trim();
			ProcessError error = new ProcessError(process,
					ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_DOWNLOAD_RESOURCE_AND_MEASURE_SPEED, action,
					ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP, errorMessage);
			logger.error("Encountered an error while downloading resource: {}", errorMessage);
			downloadResult = new DownloadResult(error);
		}
		catch (ProcessingException e)
		{
			if (e.getCause() instanceof SocketTimeoutException)
			{
				String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
				ProcessError error = new ProcessError(process,
						ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_DOWNLOAD_RESOURCE_AND_MEASURE_SPEED,
						action, ConstantsPing.POTENTIAL_FIX_URL_READ_TIMEOUT, errorMessage);
				logger.error("Encountered an error while downloading resource: {}", errorMessage);
				downloadResult = new DownloadResult(error);
			}
			else
			{
				throw e;
			}
		}
		catch (IOException e)
		{
			String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
			ProcessError error = new ProcessError(process,
					ConstantsPing.CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_DOWNLOAD_RESOURCE_AND_MEASURE_SPEED, action,
					null, errorMessage);
			logger.error("Encountered an error while downloading resource: {}", errorMessage);
			downloadResult = new DownloadResult(error);
		}
		return downloadResult;
	}

	private String toHoursMinutesSecondsMilliseconds(long millis)
	{
		long hours = (millis / 1000) / 60 / 60 % 24;
		long minutes = (millis / 1000) / 60 % 60;
		long seconds = (millis / 1000) % 60;
		long milliSeconds = millis % 1000;
		return String.format("%02d:%02d:%02d:%03d (h:m:s:ms)", hours, minutes, seconds, milliSeconds);
	}

	public static class DownloadResult
	{
		private final long downloadedBytes;
		private final long downloadedDurationMillis;
		private final ProcessError error;

		public DownloadResult(long downloadedBytes, long downloadedDurationMillis)
		{
			this.downloadedBytes = downloadedBytes;
			this.downloadedDurationMillis = downloadedDurationMillis;
			error = null;
		}

		public DownloadResult(ProcessError error)
		{
			downloadedBytes = 0;
			downloadedDurationMillis = 0;
			this.error = error;
		}

		public long getDownloadedBytes()
		{
			return downloadedBytes;
		}

		public long getDownloadedDurationMillis()
		{
			return downloadedDurationMillis;
		}

		public ProcessError getError()
		{
			return error;
		}
	}

	public static class MissingReferenceException extends Exception
	{
		public MissingReferenceException(String message)
		{
			super(message);
		}
	}
}
