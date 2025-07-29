package dev.dsf.bpe;

public enum ExecutionVariables
{
	TIMER_INTERVAL("timerInterval"),
	STOP_TIMER("stopTimer"),
	DOWNLOAD_RESOURCE_SIZE_BYTES("downloadResourceSizeBytes"),
	DOWNLOAD_RESOURCE("downloadResource"),
	DOWNLOAD_RESOURCE_REFERENCE("downloadResourceReference"),
	STATUS_CODE("statusCode"),
	ERROR("error"),
	ERROR_LIST("errors"),
	DOWNLOADED_BYTES("downloadedBytes"),
	DOWNLOADED_DURATION_MILLIS("downloadedDurationMillis"),
	PONG_TARGET_ENDPOINT_IDENTIFIER("targetEndpointIdentifier"),
	UPLOADED_BYTES("uploadedBytes"),
	UPLOADED_DURATION_MILLIS("uploadedDurationMillis"),
	RESOURCE_DOWNLOAD_ERROR("resourceDownloadError"),
	RESOURCE_UPLOAD_ERROR("resourceUploadError"),
	PING_TASK_ID("pingTaskId");

	private final String value;

	ExecutionVariables(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public String correlatedValue(String correlationKey)
	{
		return getValue() + "_" + correlationKey;
	}
}
