package dev.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.task.input.generator.DownloadedBytesGenerator;
import dev.dsf.bpe.util.task.input.generator.DownloadedDurationMillisGenerator;
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
		Integer downloadedBytes = variables
				.getInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes(correlationKey));
		Long downloadedDurationMillis = variables
				.getLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(correlationKey));

		Stream<Task.ParameterComponent> downloadedBytesParameter = downloadedBytes != null
				? Stream.of(DownloadedBytesGenerator.create(downloadedBytes))
				: Stream.empty();
		Stream<Task.ParameterComponent> downloadedDurationMillisParameter = downloadedDurationMillis != null
				? Stream.of(DownloadedDurationMillisGenerator.create(downloadedDurationMillis))
				: Stream.empty();

		return Stream.of(downloadedBytesParameter, downloadedDurationMillisParameter).flatMap(s -> s);
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
