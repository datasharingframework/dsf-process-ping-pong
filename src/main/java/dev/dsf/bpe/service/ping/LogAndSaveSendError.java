package dev.dsf.bpe.service.ping;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.service.AbstractService;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.codesystem.dsfpingstatus.CodeValueImpl;

public class LogAndSaveSendError extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveSendError.class);

	public LogAndSaveSendError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecuteWithErrorHandling(DelegateExecution execution, Variables variables) throws BpmnError
	{
		String correlationKey = variables.getTarget().getCorrelationKey();
		ProcessError error = (ProcessError) execution.getVariableLocal(ExecutionVariables.error.name());
		CodeSystem.DsfPingStatus.Code status = (CodeSystem.DsfPingStatus.Code) execution
				.getVariableLocal(ExecutionVariables.statusCode.name());
		Objects.requireNonNull(status, "status");

		ErrorListUtils.add(error, execution, correlationKey);
		variables.setVariable(ExecutionVariables.statusCode.correlatedValue(correlationKey), new CodeValueImpl(status));
		logger.debug("Saved error when trying to send ping message. Error message: {}", error.concept().getDisplay());
	}
}
