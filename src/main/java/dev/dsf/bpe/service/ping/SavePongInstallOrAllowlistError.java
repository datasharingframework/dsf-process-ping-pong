package dev.dsf.bpe.service.ping;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class SavePongInstallOrAllowlistError implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SavePongInstallOrAllowlistError.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		String correlationKey = variables.getTarget().getCorrelationKey();
		ProcessError error = new ProcessError(ConstantsPing.PROCESS_NAME_PING,
				CodeSystem.DsfPingError.Concept.REMOTE_ORG_MISSING_PONG_INSTALL_OR_LOCAL_ORG_NOT_IN_REMOTE_ALLOWLIST,
				null);
		CodeSystem.DsfPingStatus.Code status = variables.getVariableLocal(ExecutionVariables.statusCode.name());
		Objects.requireNonNull(status, "status");

		ErrorListUtils.add(error, variables, correlationKey);
		variables.setJsonVariable(ExecutionVariables.statusCode.correlatedValue(correlationKey), status);
		logger.debug("Saved error when trying to send second ping message without reference. Error message: {}",
				error.concept().getDisplay());
	}
}
