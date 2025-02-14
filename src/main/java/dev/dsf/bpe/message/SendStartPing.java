package dev.dsf.bpe.message;

import java.util.UUID;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class SendStartPing extends AbstractTaskMessageSend
{
	public SendStartPing(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected Stream<ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables)
	{
		return variables.getStartTask().getInput().stream().filter(Task.ParameterComponent::hasType)
				.filter(i -> i.getType().getCoding().stream()
						.anyMatch(c -> ConstantsPing.CODESYSTEM_DSF_PING.equals(c.getSystem())
								&& ConstantsPing.CODESYSTEM_DSF_PING_VALUE_TARGET_ENDPOINTS.equals(c.getCode())));
	}

	@Override
	protected void sendTask(DelegateExecution execution, Variables variables, Target target,
			String instantiatesCanonical, String messageName, String businessKey, String profile,
			Stream<ParameterComponent> additionalInputParameters)
	{
		// different business-key for every start-ping execution
		businessKey = UUID.randomUUID().toString();

		super.sendTask(execution, variables, target, instantiatesCanonical, messageName, businessKey, profile,
				additionalInputParameters);
	}
}
