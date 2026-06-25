package dev.dsf.bpe.service.pong;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class LogAndSaveAndStoreError implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveAndStoreError.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Target target = variables.getTarget();
		Task startTask = variables.getStartTask();

		ProcessError error = variables
				.getVariable(ExecutionVariables.resourceDownloadError.name());
		ErrorListUtils.add(error, variables);

		ProcessError errorRemote = variables
				.getVariable(ExecutionVariables.resourceDownloadErrorRemote.name());
		ErrorListUtils.addRemote(errorRemote, variables);

		PingStatusGenerator.updatePongStatusOutput(startTask,
				ErrorListUtils.getErrorList(variables).getEntries());
		variables.updateTask(startTask);

		logger.info("Error while trying to download resource from {}: {}", target.getEndpointUrl(),
				error.concept().getDisplay());
	}
}
