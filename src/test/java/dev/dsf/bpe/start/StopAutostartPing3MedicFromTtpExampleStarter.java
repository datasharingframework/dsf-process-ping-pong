package dev.dsf.bpe.start;

import java.util.Date;

import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.PingProcessPluginDefinition;
import dev.dsf.bpe.v1.constants.CodeSystems.BpmnMessage;
import dev.dsf.bpe.v1.constants.NamingSystems.OrganizationIdentifier;

public class StopAutostartPing3MedicFromTtpExampleStarter
{
	// Environment variable "DSF_CLIENT_CERTIFICATE_PATH" or args[0]: the path to the client-certificate
	// dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12
	// Environment variable "DSF_CLIENT_CERTIFICATE_PASSWORD" or args[1]: the password of the client-certificate
	// password
	public static void main(String[] args) throws Exception
	{
		ExampleStarter.forServer(args, ConstantsExampleStarters.TTP_FHIR_BASE_URL).startWith(task());
	}

	private static Task task()
	{
		var def = new PingProcessPluginDefinition();

		Task task = new Task();
		task.getMeta().addProfile(ConstantsPing.PROFILE_DSF_TASK_STOP_PING_AUTOSTART + "|" + def.getResourceVersion());
		task.setInstantiatesCanonical(
				ConstantsPing.PROFILE_DSF_TASK_STOP_PING_AUTOSTART_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setIntent(Task.TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).setIdentifier(OrganizationIdentifier
				.withValue(ConstantsExampleStarters.NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_VALUE_TTP));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier
						.withValue(ConstantsExampleStarters.NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_VALUE_TTP));

		task.addInput().setValue(new StringType(ConstantsPing.PROFILE_DSF_TASK_STOP_PING_AUTOSTART_MESSAGE_NAME))
				.getType().addCoding(BpmnMessage.messageName());

		return task;
	}
}
