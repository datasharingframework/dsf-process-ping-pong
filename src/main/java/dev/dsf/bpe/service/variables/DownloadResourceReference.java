package dev.dsf.bpe.service.variables;

import java.util.Optional;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.variables.Variables;

public final class DownloadResourceReference
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResourceReference.class);

	private DownloadResourceReference()
	{

	}

	public static void setFromTask(ProcessPluginApi api, Variables variables, Task task)
	{
		Optional<Reference> optDownloadResourceReference = api.getTaskHelper().getFirstInputParameterValue(task,
				CodeSystem.DsfPing.URL, CodeSystem.DsfPing.Code.DOWNLOAD_RESOURCE_REFERENCE.getValue(),
				Reference.class);

		if (optDownloadResourceReference.isPresent())
		{
			Reference downloadResourceReference = optDownloadResourceReference.get();
			variables.setString(ExecutionVariables.downloadResourceReference.name(),
					downloadResourceReference.getReference());

			logger.debug("Set download resource reference to {}", downloadResourceReference.getReference());
		}
		else
		{
			variables.setString(ExecutionVariables.downloadResourceReference.name(), null);
			logger.debug("No download resource reference found in task {}",
					api.getTaskHelper().getLocalVersionlessAbsoluteUrl(task));
		}
	}
}
