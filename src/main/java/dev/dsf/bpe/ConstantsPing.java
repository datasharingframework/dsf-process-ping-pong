package dev.dsf.bpe;

import java.util.List;

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

	public static final String CODESYSTEM_DSF_PING = "http://dsf.dev/fhir/CodeSystem/ping-v2";
	public static final String CODESYSTEM_DSF_PING_VALUE_PING_STATUS = "ping-status";
	public static final String CODESYSTEM_DSF_PING_VALUE_PONG_STATUS = "pong-status";
	public static final String CODESYSTEM_DSF_PING_VALUE_ENDPOINT_IDENTIFIER = "endpoint-identifier";
	public static final String CODESYSTEM_DSF_PING_VALUE_TARGET_ENDPOINTS = "target-endpoints";
	public static final String CODESYSTEM_DSF_PING_VALUE_TIMER_INTERVAL = "timer-interval";
	public static final String CODESYSTEM_DSF_PING_VALUE_DOWNLOAD_RESOURCE_SIZE_BYTES = "download-resource-size-bytes";
	public static final String CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_DURATION_MILLIS = "downloaded-duration-millis";
	public static final String CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_BYTES = "downloaded-bytes";
	public static final String CODESYSTEM_DSF_PING_VALUE_DOWNLOAD_RESOURCE_REFERENCE = "download-resource-reference";
	public static final String CODESYSTEM_DSF_PING_VALUE_ERROR = "error";

	public static final String CODESYSTEM_DSF_PING_STATUS = "http://dsf.dev/fhir/CodeSystem/ping-status-v2";
	public static final String CODESYSTEM_DSF_PING_STATUS_VALUE_COMPLETED = "completed";
	public static final String CODESYSTEM_DSF_PING_STATUS_VALUE_PENDING = "pending";
	public static final String CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR = "error";

	public static final String CODESYSTEM_DSF_PING_PROCESSES = "http://dsf.dev/fhir/CodeSystem/ping-processes-v2";
	public static final String CODESYSTEM_DSF_PING_PROCESSES_VALUE_PING = "ping";
	public static final String CODESYSTEM_DSF_PING_PROCESSES_VALUE_PONG = "pong";

	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS = "http://dsf.dev/fhir/CodeSystem/ping-process-steps-v2";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_SET_DOWNLOAD_RESOURCE_SIZE = "set-download-resource-size";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_GENERATE_AND_STORE_RESOURCE = "generate-and-store-resource";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_LOG_AND_SAVE_ERROR = "log-and-save-error";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_SELECT_TARGETS = "select-targets";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_PING = "ping";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_LOG_AND_SAVE_SEND_ERROR = "log-and-save-send-error";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_LOG_AND_SAVE_NO_RESPONSE = "log-and-save-no-response";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_SAVE_PONG = "save-pong";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_DOWNLOAD_RESOURCE_AND_MEASURE_SPEED = "download-resource-and-measure-speed";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CLEANUP_PONG = "cleanup-pong";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CLEANUP = "cleanup";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_STORE_RESULTS = "store-results";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_LOG_PING = "log-ping";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_SET_ENDPOINT_IDENTIFIER = "set-endpoint-identifier";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_SELECT_PONG_TARGET = "select-pong-target";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_STORE_DOWNLOAD_SPEED = "store-download-speed";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_LOG_AND_SAVE_AND_STORE_ERROR = "log-and-save-and-store-error";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_ESTIMATE_CLEANUP_TIMER_DURATION = "estimate-cleanup-timer-duration";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_PONG = "pong";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_STORE_UPLOAD_SPEED = "store-upload-speed";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_SAVE_TIMEOUT_ERROR = "save-timeout-error";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_STORE_ERRORS = "store-errors";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CLEANUP_TIMER_CATCH_EVENT = "cleanup-timer-catch-event";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_PONG_MESSAGE_TIMEOUT_TIMER_CATCH_EVENT = "pong-timer-catch-event";
	public static final String CODESYSTEM_DSF_PING_PROCESS_STEPS_VALUE_CHECK_PING_TASK_STATUS = "check-ping-task-status";

	public static final String CODESYSTEM_DSF_PING_UNITS = "http://dsf.dev/fhir/CodeSystem/ping-units-v2";
	public static final String CODESYSTEM_DSF_PING_UNITS_VALUE_BITS_PER_SECOND = "bits-per-second";
	public static final String CODESYSTEM_DSF_PING_UNITS_VALUE_BYTES_PER_SECOND = "bytes-per-second";
	public static final String CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABITS_PER_SECOND = "megabits-per-second";
	public static final String CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABYTES_PER_SECOND = "megabytes-per-second";

	public static final List<String> CODESYSTEM_DSF_PING_UNITS_VALUES = List.of(
			CODESYSTEM_DSF_PING_UNITS_VALUE_BITS_PER_SECOND, CODESYSTEM_DSF_PING_UNITS_VALUE_BYTES_PER_SECOND,
			CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABITS_PER_SECOND, CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABYTES_PER_SECOND);

	public static final String CODESYSTEM_READ_ACCESS_TAG = "http://dsf.dev/fhir/CodeSystem/read-access-tag";
	public static final String CODESYSTEM_READ_ACCESS_TAG_VALUE_ALL = "ALL";

	public static final String STRUCTURE_DEFINITION_URL_EXTENSION_PING_STATUS = "http://dsf.dev/fhir/StructureDefinition/extension-ping-status-v2";
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

	public static final String BPMN_EXECUTION_VARIABLE_TIMER_INTERVAL = "timerInterval";
	public static final String BPMN_EXECUTION_VARIABLE_STOP_TIMER = "stopTimer";
	public static final String BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES = "downloadResourceSizeBytes";
	public static final String BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE = "downloadResource";
	public static final String BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_REFERENCE = "downloadResourceReference";
	private static final String BPMN_EXECUTION_VARIABLE_STATUS_CODE = "statusCode";
	private static final String BPMN_EXECUTION_VARIABLE_ERROR = "error";
	private static final String BPMN_EXECUTION_VARIABLE_ERROR_LIST = "errors";
	private static final String BPMN_EXECUTION_VARIABLE_DOWNLOADED_BYTES = "downloadedBytes";
	private static final String BPMN_EXECUTION_VARIABLE_DOWNLOADED_DURATION_MILLIS = "downloadedDurationMillis";
	public static final String BPMN_EXECUTION_VARIABLE_PONG_TARGET_ENDPOINT_IDENTIFIER = "targetEndpointIdentifier";
	private static final String BPMN_EXECUTION_VARIABLE_UPLOADED_BYTES = "uploadedBytes";
	private static final String BPMN_EXECUTION_VARIABLE_UPLOADED_DURATION_MILLIS = "uploadedDurationMillis";
	public static final String BPMN_EXECUTION_VARIABLE_RESOURCE_DOWNLOAD_ERROR = "resourceDownloadError";
	public static final String BPMN_EXECUTION_VARIABLE_RESOURCE_UPLOAD_ERROR = "resourceUploadError";
	public static final String BPMN_EXECUTION_VARIABLE_PING_TASK_ID = "pingTaskId";

	public static final String BPMN_ERROR_CODE_RESOURCE_DOWNLOAD_ERROR = "resourceDownloadError";
	public static final String BPMN_ERROR_CODE_RESOURCE_UPLOAD_ERROR = "resourceUploadError";

	public static final String PONG_ERROR_MESSAGE_CLEANUP_TIMEOUT = "Timeout while waiting for cleanup message";

	public static final int DOWNLOAD_RESOURCE_SIZE_BYTES_DEFAULT = 10000000;

	public static final MediaType DOWNLOAD_RESOURCE_MIME_TYPE = MediaType.APPLICATION_OCTET_STREAM_TYPE;

	public static final String TIMER_INTERVAL_DEFAULT_VALUE = "PT24H";

	public static final String POTENTIAL_FIX_URL_DUMMY = "dsf.dev";

	public static String getBpmnExecutionVariableStatusCode()
	{
		return BPMN_EXECUTION_VARIABLE_STATUS_CODE;
	}

	public static String getBpmnExecutionVariableStatusCode(String correlationKey)
	{
		return BPMN_EXECUTION_VARIABLE_STATUS_CODE + "_" + correlationKey;
	}

	public static String getBpmnExecutionVariableError()
	{
		return BPMN_EXECUTION_VARIABLE_ERROR;
	}

	public static String getBpmnExecutionVariableErrorMessage(String correlationKey)
	{
		return BPMN_EXECUTION_VARIABLE_ERROR + "_" + correlationKey;
	}

	public static String getBpmnExecutionVariableDownloadedBytes()
	{
		return BPMN_EXECUTION_VARIABLE_DOWNLOADED_BYTES;
	}

	public static String getBpmnExecutionVariableDownloadedBytes(String correlationKey)
	{
		return BPMN_EXECUTION_VARIABLE_DOWNLOADED_BYTES + "_" + correlationKey;
	}

	public static String getBpmnExecutionVariableDownloadedDurationMillis()
	{
		return BPMN_EXECUTION_VARIABLE_DOWNLOADED_DURATION_MILLIS;
	}

	public static String getBpmnExecutionVariableDownloadedDurationMillis(String correlationKey)
	{
		return BPMN_EXECUTION_VARIABLE_DOWNLOADED_DURATION_MILLIS + "_" + correlationKey;
	}

	public static String getBpmnExecutionVariableUploadedBytes()
	{
		return BPMN_EXECUTION_VARIABLE_UPLOADED_BYTES;
	}

	public static String getBpmnExecutionVariableUploadedBytes(String correlationKey)
	{
		return BPMN_EXECUTION_VARIABLE_UPLOADED_BYTES + "_" + correlationKey;
	}

	public static String getBpmnExecutionVariableUploadedDurationMillis()
	{
		return BPMN_EXECUTION_VARIABLE_UPLOADED_DURATION_MILLIS;
	}

	public static String getBpmnExecutionVariableUploadedDurationMillis(String correlationKey)
	{
		return BPMN_EXECUTION_VARIABLE_UPLOADED_DURATION_MILLIS + "_" + correlationKey;
	}

	public static String getBpmnExecutionVariableErrorList()
	{
		return BPMN_EXECUTION_VARIABLE_ERROR_LIST;
	}

	public static String getBpmnExecutionVariableErrorMessageList(String correlationKey)
	{
		return getBpmnExecutionVariableErrorList() + "_" + correlationKey;
	}
}
