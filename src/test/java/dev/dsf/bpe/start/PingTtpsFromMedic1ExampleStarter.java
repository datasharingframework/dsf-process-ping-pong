package dev.dsf.bpe.start;

import java.util.Date;

import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsBase;
import dev.dsf.bpe.ConstantsPing;

public class PingTtpsFromMedic1ExampleStarter
{
	// Environment variable "DSF_CLIENT_CERTIFICATE_PATH" or args[0]: the path to the client-certificate
	// dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12
	// Environment variable "DSF_CLIENT_CERTIFICATE_PASSWORD" or args[1]: the password of the client-certificate
	// password
	public static void main(String[] args) throws Exception
	{
		ExampleStarter.forServer(args, ConstantsExampleStarters.MEDIC_1_FHIR_BASE_URL).startWith(task());
	}

	private static Task task()
	{
		Task task = new Task();
		task.getMeta().addProfile(ConstantsPing.PROFILE_DSF_TASK_START_PING_AND_LATEST_VERSION);
		task.setInstantiatesUri(ConstantsPing.PROFILE_DSF_TASK_PING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setIntent(Task.TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ConstantsBase.NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER)
				.setValue(ConstantsExampleStarters.NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(ConstantsBase.NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER)
				.setValue(ConstantsExampleStarters.NAMINGSYSTEM_DSF_ORGANIZATION_IDENTIFIER_VALUE_MEDIC_1);

		task.addInput().setValue(new StringType(ConstantsPing.PROFILE_DSF_TASK_START_PING_MESSAGE_NAME)).getType()
				.addCoding().setSystem(ConstantsBase.CODESYSTEM_DSF_BPMN)
				.setCode(ConstantsBase.CODESYSTEM_DSF_BPMN_VALUE_MESSAGE_NAME);

		task.addInput().setValue(new StringType(
				"OrganizationAffiliation?primary-organization:identifier=http://dsf.dev/sid/organization-identifier|highmed.org"
						+ "&role=http://dsf.dev/fhir/CodeSystem/organization-role|TTP"
						+ "&_include=OrganizationAffiliation:endpoint"))
				.getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING)
				.setCode(ConstantsPing.CODESYSTEM_DSF_PING_VALUE_TARGET_ENDPOINTS);

		return task;
	}
}
