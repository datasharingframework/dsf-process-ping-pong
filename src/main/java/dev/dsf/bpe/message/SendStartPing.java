package dev.dsf.bpe.message;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.MessageSendTask;
import dev.dsf.bpe.v2.activity.task.BusinessKeyStrategies;
import dev.dsf.bpe.v2.activity.task.BusinessKeyStrategy;
import dev.dsf.bpe.v2.activity.values.SendTaskValues;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class SendStartPing implements MessageSendTask
{
	@Override
	public List<ParameterComponent> getAdditionalInputParameters(ProcessPluginApi api, Variables variables,
			SendTaskValues sendTaskValues, Target target)
	{
		List<Task.ParameterComponent> additionalInputParameters = new ArrayList<>();

		variables.getStartTask().getInput().stream().filter(Task.ParameterComponent::hasType)
				.filter(i -> i.getType().getCoding().stream().anyMatch(c -> CodeSystem.DsfPing.URL.equals(c.getSystem())
						&& CodeSystem.DsfPing.Code.TARGET_ENDPOINTS.getValue().equals(c.getCode())))
				.forEach(additionalInputParameters::add);

		variables.getStartTask().getInput().stream().filter(this::isDownloadResourceSizeParameter)
				.forEach(additionalInputParameters::add);

		return additionalInputParameters;
	}

	private boolean isDownloadResourceSizeParameter(ParameterComponent parameterComponent)
	{
		return parameterComponent.getType().getCoding().stream().anyMatch(
				t -> CodeSystem.DsfPing.URL.equals(t.getSystem())
						&& CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_SIZE_BYTES.getValue().equals(t.getCode()));
	}

	@Override
	public BusinessKeyStrategy getBusinessKeyStrategy()
	{
		return BusinessKeyStrategies.NEW;
	}
}
