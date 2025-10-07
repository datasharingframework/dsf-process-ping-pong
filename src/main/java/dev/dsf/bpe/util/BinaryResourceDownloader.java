package dev.dsf.bpe.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

public class BinaryResourceDownloader
{
	private static final Logger logger = LoggerFactory.getLogger(BinaryResourceDownloader.class);
	private final String process;

	public BinaryResourceDownloader(String process)
	{
		this.process = process;
	}

	public DownloadResult download(Variables variables, ProcessPluginApi api, Task task)
	{
		DownloadResult downloadResult;

		long downloadResourceSizeBytes = variables.getLong(ExecutionVariables.downloadResourceSizeBytes.name());

		Optional<Reference> optDownloadResourceReference = api.getTaskHelper().getFirstInputParameterValue(task,
				CodeSystem.DsfPing.URL, CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_REFERENCE.getValue(),
				Reference.class);

		if (optDownloadResourceReference.isEmpty())
		{
			ProcessError error = new ProcessError(process,
					CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_MISSING_REFERENCE, null);
			ProcessError errorRemote = new ProcessError(process,
					CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_MISSING_REFERENCE, null);
			logDownloadError("Missing binary reference");
			downloadResult = new DownloadResult(error, errorRemote);
			return downloadResult;
		}

		Reference downloadResourceReference = optDownloadResourceReference.get();
		IdType downloadResourceReferenceIdType = new IdType(downloadResourceReference.getReference());
		String downloadResourceReferenceId = downloadResourceReferenceIdType.getIdPart();
		String webserviceUrl = downloadResourceReferenceIdType.getBaseUrl();
		try
		{
			InputStream binaryResourceInputStream = api.getFhirWebserviceClientProvider()
					.getWebserviceClient(webserviceUrl)
					.readBinary(downloadResourceReferenceId, ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE);

			try (binaryResourceInputStream)
			{
				logger.info("Downloading resource for: '{}'. Requested resource size is {} bytes...",
						downloadResourceReference.getReference(), downloadResourceSizeBytes);
				long downloadStartTime = System.currentTimeMillis();
				long downloadedBytes = skipNBytes(downloadResourceSizeBytes, binaryResourceInputStream);
				long downloadEndTime = System.currentTimeMillis();
				Duration downloadedDuration = Duration.ofMillis(downloadEndTime - downloadStartTime);
				downloadResult = new DownloadResult(downloadResourceSizeBytes, downloadedDuration);
				logger.info("Finished downloading {} bytes. Took {}", downloadedBytes, downloadedDuration.toString());
			}
			catch (IOException e)
			{
				binaryResourceInputStream.close();
				String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
				ProcessError error = new ProcessError(process,
						CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_IO_ERROR, null);
				ProcessError errorRemote = new ProcessError(process,
						CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_IO_ERROR, null);
				logDownloadError(errorMessage);
				downloadResult = new DownloadResult(error, errorRemote);
			}
		}
		catch (WebApplicationException e)
		{
			String errorMessage = (e.getResponse().getStatusInfo().getStatusCode() + " " + e.getMessage()).trim();
			int statusCode = e.getResponse().getStatus();
			ProcessError error;
			ProcessError errorRemote;

			switch (statusCode)
			{
				case 401:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_HTTP_401,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process,
							CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_HTTP_401,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				case 403:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_HTTP_403,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process,
							CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_HTTP_403,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				case 500:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_HTTP_500,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process,
							CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_HTTP_500,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				case 502:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_HTTP_502,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process,
							CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_HTTP_502,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				default:
					error = new ProcessError(process,
							CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_HTTP_UNEXPECTED, null);
					errorRemote = new ProcessError(process,
							CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_HTTP_UNEXPECTED, null);
					break;
			}
			logDownloadError(errorMessage);
			downloadResult = new DownloadResult(error, errorRemote);
		}
		catch (ProcessingException e)
		{
			if (e.getCause() instanceof SocketTimeoutException socketTimeoutException)
			{
				String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
				ProcessError error = toProcessErrorLocal(socketTimeoutException, process);
				ProcessError errorRemote = toProcessErrorRemote(socketTimeoutException, process);
				logDownloadError(errorMessage);
				downloadResult = new DownloadResult(error, errorRemote);
			}
			else if (e.getCause() instanceof ConnectTimeoutException)
			{
				String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
				ProcessError error = toProcessErrorLocalConnectTimeout(process);
				ProcessError errorRemote = toProcessErrorRemoteConnectTimeout(process);
				logDownloadError(errorMessage);
				downloadResult = new DownloadResult(error, errorRemote);
			}
			else if (e.getCause() instanceof HttpHostConnectException)
			{
				String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
				ProcessError error = toProcessErrorLocalHttpHostConnect(process);
				ProcessError errorRemote = toProcessErrorRemoteHttpHostConnect(process);
				logDownloadError(errorMessage);
				downloadResult = new DownloadResult(error, errorRemote);
			}
			else
			{
				String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
				ProcessError error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null);
				ProcessError errorRemote = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_UNKNOWN,
						null);
				logger.error("Unexpected error: {}", errorMessage);
				downloadResult = new DownloadResult(error, errorRemote);
			}
		}
		catch (IOException e)
		{
			String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
			ProcessError error = new ProcessError(process,
					CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_IO_ERROR,
					ConstantsPing.POTENTIAL_FIX_URL_READ_TIMEOUT);
			ProcessError errorRemote = new ProcessError(process,
					CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_IO_ERROR,
					ConstantsPing.POTENTIAL_FIX_URL_READ_TIMEOUT);
			logDownloadError(errorMessage);
			downloadResult = new DownloadResult(error, errorRemote);
		}
		return downloadResult;
	}

	private ProcessError toProcessErrorLocal(SocketTimeoutException timeoutException, String process)
	{
		ProcessError error;
		String message = timeoutException.getMessage().toLowerCase(Locale.ROOT);
		if (message.contains("connect"))
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_TIMEOUT_CONNECT,
					ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
		}
		else if (message.contains("read"))
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_TIMEOUT_READ,
					ConstantsPing.POTENTIAL_FIX_URL_READ_TIMEOUT);
		}
		else
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null);
			logger.error("Unexpected error: {}", timeoutException.getMessage());
		}
		return error;
	}

	private ProcessError toProcessErrorRemote(SocketTimeoutException timeoutException, String process)
	{
		ProcessError error;
		String message = timeoutException.getMessage().toLowerCase(Locale.ROOT);
		if (message.contains("connect"))
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_TIMEOUT_CONNECT,
					ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
		}
		else if (message.contains("read"))
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_TIMEOUT_READ,
					ConstantsPing.POTENTIAL_FIX_URL_READ_TIMEOUT);
		}
		else
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_UNKNOWN, null);
			logger.error("Unexpected error: {}", timeoutException.getMessage());
		}
		return error;
	}

	private ProcessError toProcessErrorLocalHttpHostConnect(String process)
	{
		return new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_HTTP_HOST_CONNECT,
				ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED);
	}

	private ProcessError toProcessErrorRemoteHttpHostConnect(String process)
	{
		return new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_HTTP_HOST_CONNECT,
				ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED);
	}

	private ProcessError toProcessErrorLocalConnectTimeout(String process)
	{
		return new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_TIMEOUT_CONNECT,
				ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
	}

	private ProcessError toProcessErrorRemoteConnectTimeout(String process)
	{
		return new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_TIMEOUT_CONNECT,
				ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
	}

	private void logDownloadError(String errorMessage)
	{
		logger.error("Encountered an error while downloading resource: {}", errorMessage);
	}

	public static class DownloadResult
	{
		private final long downloadedBytes;
		private final Duration downloadedDuration;
		private final ErrorTuple errorTuple;

		public DownloadResult(long downloadedBytes, Duration downloadedDuration)
		{
			this.downloadedBytes = downloadedBytes;
			this.downloadedDuration = downloadedDuration;
			errorTuple = null;
		}

		public DownloadResult(ProcessError error, ProcessError errorRemote)
		{
			downloadedBytes = 0;
			downloadedDuration = Duration.ZERO;
			this.errorTuple = new ErrorTuple(error, errorRemote);
		}

		public long getDownloadedBytes()
		{
			return downloadedBytes;
		}

		public Duration getDownloadedDuration()
		{
			return downloadedDuration;
		}

		public ErrorTuple getErrorTuple()
		{
			return errorTuple;
		}

		public record ErrorTuple(ProcessError errorLocal, ProcessError errorRemote)
		{
		}

	}

	private long skipNBytes(long bytes, InputStream inputStream) throws IOException
	{
		long start = bytes;
		while (bytes > 0)
		{
			long ns = inputStream.skip(bytes);
			if (ns > 0 && ns <= bytes)
			{
				// adjust number to skip
				bytes -= ns;
			}
			else if (ns == 0)
			{ // no bytes skipped
				// read one byte to check for EOS
				if (inputStream.read() == -1)
				{
					return start - bytes;
				}
				// one byte read so decrement number to skip
				bytes--;
			}
			else
			{ // skipped negative or too many bytes
				throw new IOException("Unable to skip exactly");
			}
		}

		return start;
	}
}
