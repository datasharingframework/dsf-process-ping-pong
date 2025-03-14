package dev.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.task.input.generator.NetworkSpeedMetricGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class CleanupPong extends AbstractTaskMessageSend
{
	public CleanupPong(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution,
			Variables variables)
	{
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();
		int downloadedBytes = variables.getInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes(correlationKey));
		long downloadedDurationMillis = variables.getLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(correlationKey));

		return Stream.of(NetworkSpeedMetricGenerator.createDownloadedBytes(downloadedBytes),
				NetworkSpeedMetricGenerator.createDownloadedDurationMillis(downloadedDurationMillis));
	}

	@Override
	protected void sendTask(DelegateExecution execution, Variables variables, Target target,
			String instantiatesCanonical, String messageName, String businessKey, String profile,
			Stream<Task.ParameterComponent> additionalInputParameters)
	{
		Target newTarget = new Target()
		{
			@Override
			public String getOrganizationIdentifierValue()
			{
				return target.getOrganizationIdentifierValue();
			}

			@Override
			public String getEndpointIdentifierValue()
			{
				return target.getEndpointIdentifierValue();
			}

			@Override
			public String getEndpointUrl()
			{
				return target.getEndpointUrl();
			}

			@Override
			public String getCorrelationKey()
			{
				return null;
			}
		};

		super.sendTask(execution, variables, newTarget, instantiatesCanonical, messageName, businessKey, profile,
				additionalInputParameters);
	}
}
