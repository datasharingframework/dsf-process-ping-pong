package dev.dsf.bpe;

public enum ExecutionVariables
{
	timerInterval,
	stopTimer,
	downloadResourceSizeBytes,
	maxDownloadResourceSizeBytes,
	downloadResource,
	downloadResourceReference,
	statusCode,
	statusCodeString,
	error,
	errorLocal,
	errorRemote,
	errors,
	errorsRemote,
	downloadedBytes,
	downloadedDuration,
	targetEndpointIdentifier,
	uploadedBytes,
	uploadedDuration,
	resourceDownloadError,
	resourceDownloadErrorRemote,
	resourceUploadError,
	resourceUploadErrorRemote,
	pongTimerDuration,
	pingTaskId,
	cleanupTimerDuration;

	public String correlatedValue(String correlationKey)
	{
		return name() + "_" + correlationKey;
	}
}
