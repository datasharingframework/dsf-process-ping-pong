package dev.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.fhir.authorization.read.ReadAccessHelper;
import dev.dsf.fhir.client.FhirWebserviceClientProvider;
import dev.dsf.fhir.organization.OrganizationProvider;
import dev.dsf.fhir.task.AbstractTaskMessageSend;
import dev.dsf.fhir.task.TaskHelper;

public class SendStartPing extends AbstractTaskMessageSend
{
	public SendStartPing(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider, FhirContext fhirContext)
	{
		super(clientProvider, taskHelper, readAccessHelper, organizationProvider, fhirContext);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution)
	{
		return getLeadingTaskFromExecutionVariables(execution).getInput().stream()
				.filter(Task.ParameterComponent::hasType)
				.filter(i -> i.getType().getCoding().stream()
						.anyMatch(c -> ConstantsPing.CODESYSTEM_DSF_PING.equals(c.getSystem())
								&& ConstantsPing.CODESYSTEM_DSF_PING_VALUE_TARGET_ENDPOINTS.equals(c.getCode())));
	}
}
