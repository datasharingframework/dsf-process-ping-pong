package dev.dsf.bpe.service.pong;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.service.variables.DownloadResourceReference;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class SetDownloadResourceReference implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SetDownloadResourceReference.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Setting download resource reference...");

		Task task = variables.getStartTask();

		DownloadResourceReference.setFromTask(api, variables, task);
	}
}
