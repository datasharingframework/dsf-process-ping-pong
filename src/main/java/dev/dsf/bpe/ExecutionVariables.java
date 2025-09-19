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
	pingTaskId;

	public String correlatedValue(String correlationKey)
	{
		return name() + "_" + correlationKey;
	}
}
