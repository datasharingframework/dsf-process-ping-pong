package dev.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.hl7.fhir.r4.model.DecimalType;
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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;
import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.PingProcessPluginDefinition;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceReferenceGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadResourceSizeGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedBytesGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedDurationGenerator;
import dev.dsf.bpe.util.task.input.generator.ErrorInputComponentGenerator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
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
			Arrays.asList("dsf-task-base-1.0.0.xml", "dsf-extension-error.xml", "dsf-extension-ping-status.xml",
					"dsf-task-ping.xml", "dsf-task-pong.xml", "dsf-task-start-ping.xml",
					"dsf-task-start-ping-autostart.xml", "dsf-task-stop-ping-autostart.xml",
					"dsf-task-cleanup-pong.xml"),
			Arrays.asList("dsf-read-access-tag-1.0.0.xml", "dsf-bpmn-message-1.0.0.xml", "dsf-ping-1_0.xml",
					"dsf-ping.xml", "dsf-ping-status-1_0.xml", "dsf-ping-status.xml"),
			Arrays.asList("dsf-read-access-tag-1.0.0.xml", "dsf-bpmn-message-1.0.0.xml", "dsf-ping-1_0.xml",
					"dsf-ping.xml", "dsf-ping-status-1_0.xml", "dsf-ping-status.xml", "dsf-pong-status-1_0.xml",
					"dsf-pong-status.xml", "dsf-network-speed-units.xml"));

	private ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	@Test
	public void testTaskStartAutostartProcessProfileValid()
	{
		Task task = createValidTaskStartAutostartProcess();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartAutostartProcessProfileValidWithTargetEndpoints()
	{
		Task task = createValidTaskStartAutostartProcess();
		task.addInput()
				.setValue(new StringType(
						"Endpoint?identifier=http://dsf.dev/sid/endpoint-identifier|endpoint.target.org"))
				.getType().addCoding().setSystem(CodeSystem.DsfPing.URL)
				.setCode(CodeSystem.DsfPing.Code.TARGET_ENDPOINTS.getValue())
				.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartAutostartProcessProfileValidTimerInterval()
	{
		Task task = createValidTaskStartAutostartProcess();
		task.addInput().setValue(new StringType("PT24H")).getType().addCoding().setSystem(CodeSystem.DsfPing.URL)
				.setCode(CodeSystem.DsfPing.Code.TIMER_INTERVAL.getValue())
				.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartAutostartProcessProfileNotValidTimerInterval()
	{
		Task task = createValidTaskStartAutostartProcess();
		task.addInput().setValue(new StringType("invalid_duration")).getType().addCoding()
				.setSystem(CodeSystem.DsfPing.URL).setCode(CodeSystem.DsfPing.Code.TIMER_INTERVAL.getValue())
				.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);

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

		task.addInput(DownloadResourceSizeGenerator.create(0));

		return task;
	}

	@Test
	public void testTaskStopAutostartProcessProfileValid()
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
	public void testTaskStartPingProcessProfileValid()
	{
		Task task = createValidTaskStartPingProcess();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileValidWithErrorMessages()
	{
		Task task = createValidTaskStartPingProcess();

		ProcessError.toTaskOutput(processErrors(4)).forEach(task::addOutput);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private List<ProcessError> processErrors(int amount)
	{
		List<ProcessError> errors = new ArrayList<>();
		for (int i = 0; i < amount; i++)
		{
			CodeSystem.DsfPingError.Concept[] concepts = CodeSystem.DsfPingError.Concept.values();
			errors.add(new ProcessError(ConstantsPing.PROCESS_NAME_PING, concepts[i % concepts.length], null));
		}
		return errors;
	}

	@Test
	public void testTaskStartPingProcessProfileValidWithTargetEndpoints()
	{
		Task task = createValidTaskStartPingProcess();
		task.addInput()
				.setValue(new StringType(
						"Endpoint?identifier=http://dsf.dev/sid/endpoint-identifier|endpoint.target.org"))
				.getType().addCoding().setSystem(CodeSystem.DsfPing.URL)
				.setCode(CodeSystem.DsfPing.Code.TARGET_ENDPOINTS.getValue())
				.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileValidWithBuisnessKeyOutput()
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
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(BpmnMessage.businessKey());
		task.addOutput(PingStatusGenerator.createPingStatusOutput(target, CodeSystem.DsfPingStatus.Code.PONG_MISSING,
				processErrors(5)));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileValidWithBusinessKeyAndPingStatusOutputWithDownloadAndUploadSpeeds()
			throws Exception
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
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(BpmnMessage.businessKey());
		task.addOutput(createPingStatusOutput(target, CodeSystem.DsfPingStatus.Code.PONG_RECEIVED, BigDecimal.ZERO,
				BigDecimal.ZERO, CodeSystem.DsfPingUnits.Code.bps));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid1()
	{
		Task task = createValidTaskStartPingProcess();
		task.setInstantiatesCanonical("http://dsf.dev/bpe/Process/ping/0.1.0"); // not valid

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid2()
	{
		Task task = createValidTaskStartPingProcess();
		task.setIntent(TaskIntent.FILLERORDER);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskStartPingProcessProfileNotValid3()
	{
		Task task = createValidTaskStartPingProcess();
		task.setAuthoredOn(null);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(1, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	public static Task createValidTaskStartPingProcess()
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
		task.addInput().setValue(new DecimalType(1)).getType().addCoding().setSystem(CodeSystem.DsfPing.URL)
				.setCode(CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue())
				.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);

		return task;
	}

	@Test
	public void testTaskPingValid()
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
		task.addOutput(createPongStatusOutput(target, CodeSystem.DsfPingStatus.Code.PONG_SENT));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskPingValidWithPingStatusOutputAndDownloadResourceSizeAndDownloadResourceReference()
			throws Exception
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
		task.addOutput(createPongStatusOutput(target, CodeSystem.DsfPingStatus.Code.PONG_SENT));

		task.addInput(DownloadResourceSizeGenerator.create(1000));
		task.addInput(DownloadResourceReferenceGenerator.create("https://test.endpoint.org/fhir/Binary"));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	public static Task createValidTaskPing()
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
				.getType().addCoding().setSystem(CodeSystem.DsfPing.URL)
				.setCode(CodeSystem.DsfPing.Code.ENDPOINT_IDENTIFIER.getValue())
				.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);

		return task;
	}

	@Test
	public void testTaskPongValid()
	{
		Task task = createValidTaskPong();

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskPongValidWithReferenceAndDownloadedDurationMillisAndDownloadedBytesPresent()
	{
		Task task = createValidTaskPong();

		task.addInput(DownloadResourceReferenceGenerator.create("https://test.endpoint.org/fhir/Binary"));
		task.addInput(DownloadedBytesGenerator.create(1000));
		task.addInput(DownloadedDurationGenerator.create(Duration.ofMillis(1000)));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskPongValidWithMultipleErrorMessages()
	{
		Task task = createValidTaskPong();

		task.addInput(DownloadResourceReferenceGenerator.create("https://test.endpoint.org/fhir/Binary"));
		task.addInput(DownloadedBytesGenerator.create(1000));
		task.addInput(DownloadedDurationGenerator.create(Duration.ofMillis(1000)));
		task.addInput(ErrorInputComponentGenerator.create(processErrors(1).get(0)));
		task.addInput(ErrorInputComponentGenerator.create(processErrors(1).get(0)));

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testTaskCleanupPongValid()
	{
		Task task = createValidTaskCleanupPong();

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

	private Task createValidTaskCleanupPong()
	{
		Task task = new Task();
		task.getMeta().addProfile(ConstantsPing.PROFILE_DSF_TASK_CLEANUP_PONG);
		task.setInstantiatesCanonical(
				ConstantsPing.PROFILE_DSF_TASK_CLEANUP_PONG_PROCESS_URI + "|" + def.getResourceVersion());
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("DIC 1"));
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
				.setIdentifier(OrganizationIdentifier.withValue("TTP"));

		task.addInput().setValue(new StringType(ConstantsPing.PROFILE_DSF_TASK_CLEANUP_PONG_MESSAGE_NAME)).getType()
				.addCoding(BpmnMessage.messageName());
		task.addInput().setValue(new StringType(UUID.randomUUID().toString())).getType()
				.addCoding(BpmnMessage.businessKey());

		task.addInput(DownloadedBytesGenerator.create(1000));
		task.addInput(DownloadedDurationGenerator.create(Duration.ofMillis(1000)));
		return task;
	}

	@Test
	public void testDraftTaskStartPingValid() throws IOException
	{
		FhirContext ctx = FhirContext.forR4();
		InputStream fileInputStream = getClass().getClassLoader()
				.getResourceAsStream("fhir/Task/dsf-task-start-ping.xml");
		String xml = new String(fileInputStream.readAllBytes());
		xml = fillPlaceholders(xml);
		fileInputStream.close();

		IParser parser = ctx.newXmlParser();
		Task task = parser.parseResource(Task.class, xml);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testDraftTaskStartPingAutostartValid() throws IOException
	{
		FhirContext ctx = FhirContext.forR4();
		InputStream fileInputStream = getClass().getClassLoader()
				.getResourceAsStream("fhir/Task/dsf-task-start-ping-autostart.xml");
		String xml = new String(fileInputStream.readAllBytes());
		xml = fillPlaceholders(xml);
		fileInputStream.close();

		IParser parser = ctx.newXmlParser();
		Task task = parser.parseResource(Task.class, xml);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	@Test
	public void testDraftTaskStopPingAutostartValid() throws IOException
	{
		FhirContext ctx = FhirContext.forR4();
		InputStream fileInputStream = getClass().getClassLoader()
				.getResourceAsStream("fhir/Task/dsf-task-stop-ping-autostart.xml");
		String xml = new String(fileInputStream.readAllBytes());
		xml = fillPlaceholders(xml);
		fileInputStream.close();

		IParser parser = ctx.newXmlParser();
		Task task = parser.parseResource(Task.class, xml);

		ValidationResult result = resourceValidator.validate(task);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());
	}

	private String fillPlaceholders(String xml)
	{
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		xml = xml.replaceAll("#\\{version}", def.getResourceVersion());
		xml = xml.replaceAll("#\\{date}",
				dtf.format(LocalDate.ofInstant(Instant.now(), TimeZone.getDefault().toZoneId())));
		return xml;
	}

	private Task.TaskOutputComponent createPingStatusOutput(Target target, CodeSystem.DsfPingStatus.Code statusCode,
			BigDecimal downloadSpeed, BigDecimal uploadSpeed, CodeSystem.DsfPingUnits.Code unit)
	{
		return PingStatusGenerator.createPingStatusOutput(target, statusCode, null, downloadSpeed, unit, uploadSpeed,
				unit);
	}

	private Task.TaskOutputComponent createPongStatusOutput(Target target, CodeSystem.DsfPingStatus.Code statusCode)
	{
		return PingStatusGenerator.createPongStatusOutput(target, statusCode, null);
	}
}
