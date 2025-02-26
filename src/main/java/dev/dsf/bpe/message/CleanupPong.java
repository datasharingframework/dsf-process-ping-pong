package dev.dsf.bpe.message;

import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.util.task.input.generator.NetworkSpeedMetricGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
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
		NetworkSpeedMetricGenerator networkSpeedMetricGenerator = new NetworkSpeedMetricGenerator();
		return Stream.of(networkSpeedMetricGenerator.createDownloadedBytes(0),
				networkSpeedMetricGenerator.createDownloadedDurationMillis(0));
	}
}
