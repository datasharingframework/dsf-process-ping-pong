package dev.dsf.bpe;


public interface ConstantsPing
{
	String PROCESS_NAME_PING_AUTOSTART = "pingAutostart";
	String PROCESS_NAME_PING = "ping";
	String PROCESS_NAME_PONG = "pong";

	String PROCESS_NAME_FULL_PING_AUTOSTART = "dsfdev_" + PROCESS_NAME_PING_AUTOSTART;
	String PROCESS_NAME_FULL_PING = "dsfdev_" + PROCESS_NAME_PING;
	String PROCESS_NAME_FULL_PONG = "dsfdev_" + PROCESS_NAME_PONG;

	String PROCESS_DSF_URI_BASE = "http://dsf.dev/bpe/Process/";

	String PROFILE_DSF_TASK_START_PING_AUTOSTART = "http://dsf.dev/fhir/StructureDefinition/task-start-ping-autostart";
	String PROFILE_DSF_TASK_START_PING_AUTOSTART_PROCESS_URI = PROCESS_DSF_URI_BASE + PROCESS_NAME_PING_AUTOSTART;
	String PROFILE_DSF_TASK_START_PING_AUTOSTART_MESSAGE_NAME = "startPingAutostart";

	String PROFILE_DSF_TASK_STOP_PING_AUTOSTART = "http://dsf.dev/fhir/StructureDefinition/task-stop-ping-autostart";
	String PROFILE_DSF_TASK_STOP_PING_AUTOSTART_PROCESS_URI = PROCESS_DSF_URI_BASE + PROCESS_NAME_PING_AUTOSTART;
	String PROFILE_DSF_TASK_STOP_PING_AUTOSTART_MESSAGE_NAME = "stopPingAutostart";

	String PROFILE_DSF_TASK_START_PING = "http://dsf.dev/fhir/StructureDefinition/task-start-ping";
	String PROFILE_DSF_TASK_START_PING_MESSAGE_NAME = "startPing";

	String PROFILE_DSF_TASK_PING = "http://dsf.dev/fhir/StructureDefinition/task-ping";
	String PROFILE_DSF_TASK_PING_PROCESS_URI = PROCESS_DSF_URI_BASE + PROCESS_NAME_PING;
	String PROFILE_DSF_TASK_PING_MESSAGE_NAME = "ping";

	String PROFILE_DSF_TASK_PONG_TASK = "http://dsf.dev/fhir/StructureDefinition/task-pong";
	String PROFILE_DSF_TASK_PONG_PROCESS_URI = PROCESS_DSF_URI_BASE + PROCESS_NAME_PONG;
	String PROFILE_DSF_TASK_PONG_MESSAGE_NAME = "pong";

	String CODESYSTEM_DSF_PING = "http://dsf.dev/fhir/CodeSystem/ping";
	String CODESYSTEM_DSF_PING_VALUE_PING_STATUS = "ping-status";
	String CODESYSTEM_DSF_PING_VALUE_PONG_STATUS = "pong-status";
	String CODESYSTEM_DSF_PING_VALUE_ENDPOINT_IDENTIFIER = "endpoint-identifier";
	String CODESYSTEM_DSF_PING_VALUE_TARGET_ENDPOINTS = "target-endpoints";
	String CODESYSTEM_DSF_PING_VALUE_TIMER_INTERVAL = "timer-interval";

	String CODESYSTEM_DSF_PING_STATUS = "http://dsf.dev/fhir/CodeSystem/ping-status";
	String CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED = "not-allowed";
	String CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE = "not-reachable";
	String CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_MISSING = "pong-missing";
	String CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_RECEIVED = "pong-received";
	String CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_SEND = "pong-send";

	String EXTENSION_URL_PING_STATUS = "http://dsf.dev/fhir/StructureDefinition/extension-ping-status";
	String EXTENSION_URL_CORRELATION_KEY = "correlation-key";
	String EXTENSION_URL_ORGANIZATION_IDENTIFIER = "organization-identifier";
	String EXTENSION_URL_ENDPOINT_IDENTIFIER = "endpoint-identifier";
	String EXTENSION_URL_ERROR_MESSAGE = "error-message";

	String BPMN_EXECUTION_VARIABLE_TIMER_INTERVAL = "timerInterval";
	String BPMN_EXECUTION_VARIABLE_STOP_TIMER = "stopTimer";

	String TIMER_INTERVAL_DEFAULT_VALUE = "PT24H";
}
