package dev.dsf.bpe.service.ping;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class LogAndSaveSendError implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveSendError.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		String correlationKey = variables.getTarget().getCorrelationKey();
		ProcessError error = variables.getVariableLocal(ExecutionVariables.error.name());
		CodeSystem.DsfPingStatus.Code status = variables
				.getVariableLocal(ExecutionVariables.statusCode.name());
		Objects.requireNonNull(status, "status");

		ErrorListUtils.add(error, variables, correlationKey);
		variables.setJsonVariable(ExecutionVariables.statusCode.correlatedValue(correlationKey), status);
		logger.debug("Saved error when trying to send ping message. Error message: {}", error.concept().getDisplay());
	}
}
