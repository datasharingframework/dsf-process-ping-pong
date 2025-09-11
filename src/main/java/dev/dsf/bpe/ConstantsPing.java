package dev.dsf.bpe;

import jakarta.ws.rs.core.MediaType;

public final class ConstantsPing
{
	private ConstantsPing()
	{
	}

	public static final String PROCESS_NAME_PING_AUTOSTART = "pingAutostart";
	public static final String PROCESS_NAME_PING = "ping";
	public static final String PROCESS_NAME_PONG = "pong";

	public static final String PROCESS_NAME_FULL_PING_AUTOSTART = "dsfdev_" + PROCESS_NAME_PING_AUTOSTART;
	public static final String PROCESS_NAME_FULL_PING = "dsfdev_" + PROCESS_NAME_PING;
	public static final String PROCESS_NAME_FULL_PONG = "dsfdev_" + PROCESS_NAME_PONG;

	public static final String PROCESS_DSF_URI_BASE = "http://dsf.dev/bpe/Process/";

	public static final String PROFILE_DSF_TASK_START_PING_AUTOSTART = "http://dsf.dev/fhir/StructureDefinition/task-start-ping-autostart";
	public static final String PROFILE_DSF_TASK_START_PING_AUTOSTART_PROCESS_URI = PROCESS_DSF_URI_BASE
			+ PROCESS_NAME_PING_AUTOSTART;
	public static final String PROFILE_DSF_TASK_START_PING_AUTOSTART_MESSAGE_NAME = "startPingAutostart";

	public static final String PROFILE_DSF_TASK_STOP_PING_AUTOSTART = "http://dsf.dev/fhir/StructureDefinition/task-stop-ping-autostart";
	public static final String PROFILE_DSF_TASK_STOP_PING_AUTOSTART_PROCESS_URI = PROCESS_DSF_URI_BASE
			+ PROCESS_NAME_PING_AUTOSTART;
	public static final String PROFILE_DSF_TASK_STOP_PING_AUTOSTART_MESSAGE_NAME = "stopPingAutostart";

	public static final String PROFILE_DSF_TASK_START_PING = "http://dsf.dev/fhir/StructureDefinition/task-start-ping";
	public static final String PROFILE_DSF_TASK_START_PING_MESSAGE_NAME = "startPing";

	public static final String PROFILE_DSF_TASK_PING = "http://dsf.dev/fhir/StructureDefinition/task-ping";
	public static final String PROFILE_DSF_TASK_PING_PROCESS_URI = PROCESS_DSF_URI_BASE + PROCESS_NAME_PING;
	public static final String PROFILE_DSF_TASK_PING_MESSAGE_NAME = "ping";

	public static final String PROFILE_DSF_TASK_PONG_TASK = "http://dsf.dev/fhir/StructureDefinition/task-pong";
	public static final String PROFILE_DSF_TASK_PONG_PROCESS_URI = PROCESS_DSF_URI_BASE + PROCESS_NAME_PONG;
	public static final String PROFILE_DSF_TASK_PONG_MESSAGE_NAME = "pong";

	public static final String PROFILE_DSF_TASK_CLEANUP_PONG = "http://dsf.dev/fhir/StructureDefinition/task-cleanup-pong";
	public static final String PROFILE_DSF_TASK_CLEANUP_PONG_PROCESS_URI = PROCESS_DSF_URI_BASE + PROCESS_NAME_PONG;
	public static final String PROFILE_DSF_TASK_CLEANUP_PONG_MESSAGE_NAME = "cleanupPong";

	public static final String STRUCTURE_DEFINITION_URL_EXTENSION_PING_STATUS = "http://dsf.dev/fhir/StructureDefinition/extension-ping-status";
	public static final String STRUCTURE_DEFINITION_URL_EXTENSION_NETWORK_SPEED = "http://dsf.dev/fhir/StructureDefinition/extension-network-speed";
	public static final String STRUCTURE_DEFINITION_URL_EXTENSION_ERROR = "http://dsf.dev/fhir/StructureDefinition/extension-error";

	public static final String EXTENSION_URL_CORRELATION_KEY = "correlation-key";
	public static final String EXTENSION_URL_ORGANIZATION_IDENTIFIER = "organization-identifier";
	public static final String EXTENSION_URL_ENDPOINT_IDENTIFIER = "endpoint-identifier";
	public static final String EXTENSION_URL_DOWNLOAD_SPEED = "download-speed";
	public static final String EXTENSION_URL_UPLOAD_SPEED = "upload-speed";
	public static final String EXTENSION_URL_NETWORK_SPEED_UNIT = "unit";
	public static final String EXTENSION_URL_NETWORK_SPEED_VALUE = "network-speed";
	public static final String EXTENSION_URL_ERROR = "error";
	public static final String EXTENSION_URL_PROCESS = "process";
	public static final String EXTENSION_URL_PROCESS_STEP = "process-step";
	public static final String EXTENSION_URL_ACTION = "action";
	public static final String EXTENSION_URL_POTENTIAL_FIX = "potential-fix";
	public static final String EXTENSION_URL_MESSAGE = "message";

	public static final long DOWNLOAD_RESOURCE_SIZE_BYTES_DEFAULT = 10000000L;

	public static final MediaType DOWNLOAD_RESOURCE_MIME_TYPE = MediaType.APPLICATION_OCTET_STREAM_TYPE;

	public static final String TIMER_INTERVAL_DEFAULT_VALUE = "PT24H";

	public static final String POTENTIAL_FIX_URL_BASE = "https://dsf.dev/s";
	public static final String POTENTIAL_FIX_URL_ERROR_HTTP = POTENTIAL_FIX_URL_BASE + "/error-http";
	public static final String POTENTIAL_FIX_URL_READ_TIMEOUT = POTENTIAL_FIX_URL_BASE + "/read-timeout";
	public static final String POTENTIAL_FIX_URL_ERROR_SSL = POTENTIAL_FIX_URL_BASE + "/error-ssl";
	public static final String POTENTIAL_FIX_URL_CONNECTION_TIMEOUT = POTENTIAL_FIX_URL_BASE + "/connection-timeout";
	public static final String POTENTIAL_FIX_URL_CONNECTION_REFUSED = POTENTIAL_FIX_URL_BASE + "/connection-refused";
	public static final String POTENTIAL_FIX_URL_UNKNOWN_HOST = POTENTIAL_FIX_URL_BASE + "/unknown-host";

	public static final String CLEANUP_ERROR_ACTION = "Deleting generated Binary resource from local DSF FHIR server.";
}
