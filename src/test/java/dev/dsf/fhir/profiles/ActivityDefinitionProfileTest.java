package dev.dsf.fhir.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Arrays;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;
import dev.dsf.bpe.PingProcessPluginDefinition;
import dev.dsf.fhir.authorization.process.ProcessAuthorizationHelper;
import dev.dsf.fhir.authorization.process.ProcessAuthorizationHelperImpl;
import dev.dsf.fhir.validation.ResourceValidator;
import dev.dsf.fhir.validation.ResourceValidatorImpl;
import dev.dsf.fhir.validation.ValidationSupportRule;

public class ActivityDefinitionProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(ActivityDefinitionProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(
			PingProcessPluginDefinition.VERSION, PingProcessPluginDefinition.RELEASE_DATE,
			Arrays.asList("dsf-activity-definition-0.5.0.xml", "dsf-extension-process-authorization-0.5.0.xml",
					"dsf-extension-process-authorization-consortium-role-0.5.0.xml",
					"dsf-extension-process-authorization-organization-0.5.0.xml",
					"dsf-coding-process-authorization-local-all-0.5.0.xml",
					"dsf-coding-process-authorization-local-consortium-role-0.5.0.xml",
					"dsf-coding-process-authorization-local-organization-0.5.0.xml",
					"dsf-coding-process-authorization-remote-all-0.5.0.xml",
					"dsf-coding-process-authorization-remote-consortium-role-0.5.0.xml",
					"dsf-coding-process-authorization-remote-organization-0.5.0.xml"),
			Arrays.asList("dsf-read-access-tag-0.5.0.xml", "dsf-process-authorization-0.5.0.xml"),
			Arrays.asList("dsf-read-access-tag-0.5.0.xml", "dsf-process-authorization-recipient-0.5.0.xml",
					"dsf-process-authorization-requester-0.5.0.xml"));

	private final ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	private final ProcessAuthorizationHelper processAuthorizationHelper = new ProcessAuthorizationHelperImpl();

	@Test
	public void testAutostartValid() throws Exception
	{
		ActivityDefinition ad = validationRule
				.readActivityDefinition(Paths.get("src/main/resources/fhir/ActivityDefinition/dsf-ping-autostart.xml"));

		ValidationResult result = resourceValidator.validate(ad);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());

		assertTrue(processAuthorizationHelper.isValid(ad, taskProfile -> true, orgIdentifier -> true, role -> true));
	}

	@Test
	public void testPingValid() throws Exception
	{
		ActivityDefinition ad = validationRule
				.readActivityDefinition(Paths.get("src/main/resources/fhir/ActivityDefinition/dsf-ping.xml"));

		ValidationResult result = resourceValidator.validate(ad);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());

		assertTrue(processAuthorizationHelper.isValid(ad, taskProfile -> true, orgIdentifier -> true, role -> true));
	}

	@Test
	public void testPongValid() throws Exception
	{
		ActivityDefinition ad = validationRule
				.readActivityDefinition(Paths.get("src/main/resources/fhir/ActivityDefinition/dsf-pong.xml"));

		ValidationResult result = resourceValidator.validate(ad);
		ValidationSupportRule.logValidationMessages(logger, result);

		assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
				|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());

		assertTrue(processAuthorizationHelper.isValid(ad, taskProfile -> true, orgIdentifier -> true, role -> true));
	}
}
