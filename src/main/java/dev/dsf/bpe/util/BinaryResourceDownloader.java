package dev.dsf.bpe.util;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryResourceDownloader
{
	private static final Logger logger = LoggerFactory.getLogger(BinaryResourceDownloader.class);

	public DownloadResult download(InputStream inputStream, String reference, int downloadResourceSizeBytes, int maxDownloadSizeBytes)
	{
		DownloadResult downloadResult;
		try (inputStream)
		{
			logger.info(
					"Starting resource download for: '{}'. Requested resource size is {} bytes, maximum downloadable size is {} bytes",
					reference, downloadResourceSizeBytes, maxDownloadSizeBytes);
			long downloadStartTime = System.currentTimeMillis();
			int bufferSize = Math.min(downloadResourceSizeBytes, maxDownloadSizeBytes);
			byte[] buffer = new byte[bufferSize];
			int bytesRead = inputStream.read(buffer);
			long downloadEndTime = System.currentTimeMillis();
			long downloadedDurationMillis = downloadEndTime - downloadStartTime;
			downloadResult = new DownloadResult(bytesRead, downloadedDurationMillis);
			logger.info("Finished downloading {} bytes. Took {}", bytesRead, toHoursMinutesSecondsMilliseconds(downloadedDurationMillis));

		} catch (IOException e)
		{
			downloadResult = new DownloadResult(e.getMessage());
		}
		return downloadResult;
	}


	private String toHoursMinutesSecondsMilliseconds(long millis)
	{
		long hours = (millis / 1000) / 60 / 60 % 24;
		long minutes = (millis / 1000) / 60 % 60;
		long seconds = (millis / 1000) % 60;
		long milliSeconds = millis % 1000;
		return String.format("%02d:%02d:%02d:%02dms", hours, minutes, seconds, milliSeconds);
	}

	public static class DownloadResult {
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
