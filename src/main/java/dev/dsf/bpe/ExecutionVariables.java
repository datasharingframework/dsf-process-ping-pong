package dev.dsf.bpe;

public enum ExecutionVariables
{
	timerInterval,
	downloadResourceSizeBytes,
	maxDownloadResourceSizeBytes,
	downloadResourceReference,
	statusCode,
	statusCodeString,
	rawHttpStatus,
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
	cleanupTimerDuration,
	sendPong,
	pingAttempt,
	pongAttempt;

	public String correlatedValue(String correlationKey)
	{
		return name() + "_" + correlationKey;
	}
}
