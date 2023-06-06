package dev.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.PingProcessPluginDefinition;
import dev.dsf.bpe.util.PingStatusGenerator;
import dev.dsf.bpe.v1.constants.CodeSystems.BpmnMessage;
import dev.dsf.bpe.v1.constants.NamingSystems.EndpointIdentifier;
import dev.dsf.bpe.v1.constants.NamingSystems.OrganizationIdentifier;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.fhir.validation.ResourceValidator;
import dev.dsf.fhir.validation.ResourceValidatorImpl;
import dev.dsf.fhir.validation.ValidationSupportRule;

public class TaskProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(TaskProfileTest.class);

	private static final PingProcessPluginDefinition def = new PingProcessPluginDefinition();

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(def.getResourceVersion(),
			def.getResourceReleaseDate(),
			Arrays.asList("dsf-task-base-1.0.0.xml", "dsf-extension-ping-status.xml", "dsf-task-ping.xml",
					"dsf-task-pong.xml", "dsf-task-start-ping.xml", "dsf-task-start-ping-autostart.xml",
					"dsf-task-stop-ping-autostart.xml"),
			Arrays.asList("dsf-read-access-tag-1.0.0.xml", "dsf-bpmn-message-1.0.0.xml", "dsf-ping.xml",
					"dsf-ping-status.xml"),
			Arrays.asList("dsf-read-access-tag-1.0.0.xml", "dsf-bpmn-message-1.0.0.xml", "dsf-ping.xml",
					"dsf-ping-status.xml", "dsf-pong-status.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testTaskStartAutostartProcessProfileValid() throws Exception
	{
		Task task = createValidTaskStartAutostartProcess();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartAutostartProcessProfileValidWithTargetEndpoints() throws Exception
	{
		Task task = createValidTaskStartAutostartProcess();
		task.addInput()
				.setValue(new StringType(
						"Endpoint?identifier=http://dsf.dev/sid/endpoint-identifier|endpoint.target.org"))
				.getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING)
				.setCode(ConstantsPing.CODESYSTEM_DSF_PING_VALUE_TARGET_ENDPOINTS);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartAutostartProcessProfileValidTimerInterval() throws Exception
	{
		Task task = createValidTaskStartAutostartProcess();
		task.addInput().setValue(new StringType("PT24H")).getType().addCoding()
				.setSystem(ConstantsPing.CODESYSTEM_DSF_PING)
				.setCode(ConstantsPing.CODESYSTEM_DSF_PING_VALUE_TIMER_INTERVAL);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartAutostartProcessProfileNotValidTimerInterval() throws Exception
	{
		Task task = createValidTaskStartAutostartProcess();
		task.addInput().setValue(new StringType("invalid_duration")).getType().addCoding()
				.setSystem(ConstantsPing.CODESYSTEM_DSF_PING)
				.setCode(ConstantsPing.CODESYSTEM_DSF_PING_VALUE_TIMER_INTERVAL);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskStartAutostartProcess()
	{
		Task task = new Task();
		task.getMeta().addProfile(ConstantsPing.PROFILE_DSF_TASK_START_PING_AUTOSTART);
		task.setInstantiatesCanonical(
				ConstantsPing.PROFILE_DSF_TASK_START_PING_AUTOSTART_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("TTP"));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("TTP"));

		task.addInput().setValue(new StringType(ConstantsPing.PROFILE_DSF_TASK_START_PING_AUTOSTART_MESSAGE_NAME))
				.getType().addCoding(BpmnMessage.messageName());

		return task;
	}

	@Test
	public void testTaskStopAutostartProcessProfileValid() throws Exception
	{
		Task task = createValidTaskStopAutostartProcess();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskStopAutostartProcess()
	{
		Task task = new Task();
		task.getMeta().addProfile(ConstantsPing.PROFILE_DSF_TASK_STOP_PING_AUTOSTART);
		task.setInstantiatesCanonical(
				ConstantsPing.PROFILE_DSF_TASK_STOP_PING_AUTOSTART_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("TTP"));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("TTP"));

		task.addInput().setValue(new StringType(ConstantsPing.PROFILE_DSF_TASK_STOP_PING_AUTOSTART_MESSAGE_NAME))
				.getType().addCoding(BpmnMessage.messageName());

		return task;
	}

	@Test
	public void testTaskStartPingProcessProfileValid() throws Exception
	{
		Task task = createValidTaskStartPingProcess();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileValidWithTargetEndpoints() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.addInput()
				.setValue(new StringType(
						"Endpoint?identifier=http://dsf.dev/sid/endpoint-identifier|endpoint.target.org"))
				.getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING)
				.setCode(ConstantsPing.CODESYSTEM_DSF_PING_VALUE_TARGET_ENDPOINTS);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileValidWithBuisnessKeyOutput() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.addOutput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(BpmnMessage.businessKey());

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileValidWithBusinessKeyAndPingStatusOutput() throws Exception
	{
		Target target = new Target()
		{
			@Override
			public String getOrganizationIdentifierValue()
			{
				return "target.org";
			}

			@Override
			public String getEndpointUrl()
			{
				return "https://endpoint.target.org/fhir";
			}

			@Override
			public String getEndpointIdentifierValue()
			{
				return "endpoint.target.org";
			}

			@Override
			public String getCorrelationKey()
			{
				return UUID.randomUUID().toString();
			}
		};

		Task task = createValidTaskStartPingProcess();
		task.addOutput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(BpmnMessage.businessKey());
		task.addOutput(new PingStatusGenerator().createPingStatusOutput(target,
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE, "some error message"));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid1() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setInstantiatesCanonical("http://dsf.dev/bpe/Process/ping/0.1.0"); // not valid

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid2() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setIntent(TaskIntent.FILLERORDER);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid3() throws Exception
	{
		Task task = createValidTaskStartPingProcess();
		task.setAuthoredOn(null);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskStartPingProcess()
	{
		Task task = new Task();
		task.getMeta().addProfile(ConstantsPing.PROFILE_DSF_TASK_START_PING);
		task.setInstantiatesCanonical(ConstantsPing.PROFILE_DSF_TASK_PING_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("TTP"));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("TTP"));

		task.addInput().setValue(new StringType(ConstantsPing.PROFILE_DSF_TASK_START_PING_MESSAGE_NAME)).getType()
				.addCoding(BpmnMessage.messageName());

		return task;
	}

	@Test
	public void testTaskPingValid() throws Exception
	{
		Task task = createValidTaskPing();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskPingValidWithPingStatusOutput() throws Exception
	{
		Target target = new Target()
		{
			@Override
			public String getOrganizationIdentifierValue()
			{
				return "target.org";
			}

			@Override
			public String getEndpointUrl()
			{
				return "https://endpoint.target.org/fhir";
			}

			@Override
			public String getEndpointIdentifierValue()
			{
				return "endpoint.target.org";
			}

			@Override
			public String getCorrelationKey()
			{
				return UUID.randomUUID().toString();
			}
		};
		Task task = createValidTaskPing();
		task.addOutput(new PingStatusGenerator().createPongStatusOutput(target,
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_SEND));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskPing()
	{
		Task task = new Task();
		task.getMeta().addProfile(ConstantsPing.PROFILE_DSF_TASK_PING);
		task.setInstantiatesCanonical(ConstantsPing.PROFILE_DSF_TASK_PONG_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("TTP"));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("DIC 1"));

		task.addInput().setValue(new StringType(ConstantsPing.PROFILE_DSF_TASK_PING_MESSAGE_NAME)).getType()
				.addCoding(BpmnMessage.messageName());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(BpmnMessage.businessKey());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(BpmnMessage.correlationKey());
		task.addInput()
				.setValue(new Reference().setType(ResourceType.Endpoint.name())
						.setIdentifier(EndpointIdentifier.withValue("endpoint.target.org")))
				.getType().addCoding().setSystem(ConstantsPing.CODESYSTEM_DSF_PING)
				.setCode(ConstantsPing.CODESYSTEM_DSF_PING_VALUE_ENDPOINT_IDENTIFIER);

		return task;
	}

	@Test
	public void testTaskPongValid() throws Exception
	{
		Task task = createValidTaskPong();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private Task createValidTaskPong()
	{
		Task task = new Task();
		task.getMeta().addProfile(ConstantsPing.PROFILE_DSF_TASK_PONG_TASK);
		task.setInstantiatesCanonical(ConstantsPing.PROFILE_DSF_TASK_PING_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("DIC 1"));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("TTP"));

		task.addInput().setValue(new StringType(ConstantsPing.PROFILE_DSF_TASK_PONG_MESSAGE_NAME)).getType()
				.addCoding(BpmnMessage.messageName());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(BpmnMessage.businessKey());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(BpmnMessage.correlationKey());

		return task;
	}
}
