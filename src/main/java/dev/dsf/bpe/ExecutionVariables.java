package dev.dsf.bpe;

public enum ExecutionVariables
{
	timerInterval,
	stopTimer,
	downloadResourceSizeBytes,
	downloadResource,
	downloadResourceReference,
	statusCode,
	error,
	errors,
	downloadedBytes,
	downloadedDuration,
	targetEndpointIdentifier,
	uploadedBytes,
	uploadedDuration,
	resourceDownloadError,
	resourceUploadError,
	pingTaskId;

	public String correlatedValue(String correlationKey)
	{
		return name() + "_" + correlationKey;
	}
}
