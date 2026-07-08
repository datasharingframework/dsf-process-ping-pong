package dev.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import dev.dsf.bpe.spring.config.PingConfig;
import dev.dsf.bpe.v2.AbstractProcessPluginDefinition;

public class PingProcessPluginDefinition extends AbstractProcessPluginDefinition
{
	@Override
	public List<String> getProcessModels()
	{
		return List.of("bpe/ping-autostart.bpmn", "bpe/ping.bpmn", "bpe/pong.bpmn");
	}

	@Override
	public List<Class<?>> getSpringConfigurations()
	{
		return List.of(PingConfig.class);
	}

	@Override
	public Map<String, List<String>> getFhirResourcesByProcessId()
	{
		var aPing = "fhir/ActivityDefinition/dsf-ping.xml";
		var aPingAutostart = "fhir/ActivityDefinition/dsf-ping-autostart.xml";
		var aPong = "fhir/ActivityDefinition/dsf-pong.xml";

		var cPing = "fhir/CodeSystem/dsf-ping.xml";
		var cPingStatus = "fhir/CodeSystem/dsf-ping-status.xml";
		var cPingError = "fhir/CodeSystem/dsf-ping-error.xml";

		var sPingStatus = "fhir/StructureDefinition/dsf-extension-ping-status.xml";
		var sPing = "fhir/StructureDefinition/dsf-task-ping.xml";
		var sPong = "fhir/StructureDefinition/dsf-task-pong.xml";
		var sStartPing = "fhir/StructureDefinition/dsf-task-start-ping.xml";
		var sStartPingAutostart = "fhir/StructureDefinition/dsf-task-start-ping-autostart.xml";
		var sStopPingAutostart = "fhir/StructureDefinition/dsf-task-stop-ping-autostart.xml";
		var sCleanupPong = "fhir/StructureDefinition/dsf-task-cleanup-pong.xml";
		var sErrorExtension = "fhir/StructureDefinition/dsf-extension-error.xml";

		var tStartPing = "fhir/Task/dsf-task-start-ping.xml";
		var tStartPingAutoStart = "fhir/Task/dsf-task-start-ping-autostart.xml";
		var tStopPingAutoStart = "fhir/Task/dsf-task-stop-ping-autostart.xml";

		var vPing = "fhir/ValueSet/dsf-ping.xml";
		var vPingUnits = "fhir/ValueSet/dsf-network-speed-units.xml";
		var vPingStatus = "fhir/ValueSet/dsf-ping-status.xml";
		var vPongStatus = "fhir/ValueSet/dsf-pong-status.xml";

		return Map.of(ConstantsPing.PROCESS_NAME_FULL_PING,
				Arrays.asList(aPing, cPing, cPingStatus, cPingError, sErrorExtension, sPingStatus, sStartPing, sPong,
						sCleanupPong, tStartPing, vPing, vPingStatus, vPingUnits),
				ConstantsPing.PROCESS_NAME_FULL_PING_AUTOSTART,
				Arrays.asList(aPingAutostart, cPing, sStartPingAutostart, sStopPingAutostart, tStartPingAutoStart,
						tStopPingAutoStart, vPing),
				ConstantsPing.PROCESS_NAME_FULL_PONG, Arrays.asList(aPong, cPing, cPingStatus, cPingError,
						sErrorExtension, sPingStatus, sPing, vPing, vPongStatus, vPingUnits));
	}
}
