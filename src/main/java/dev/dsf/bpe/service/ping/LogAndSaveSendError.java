package dev.dsf.bpe.service.ping;

import java.time.Duration;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.duration.DurationValueImpl;

public class LogAndSaveSendError extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveSendError.class);

	public LogAndSaveSendError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError
	{
		String correlationKey = variables.getTarget().getCorrelationKey();
		ProcessError error = (ProcessError) execution.getVariableLocal(ExecutionVariables.error.name());
		ErrorListUtils.add(error, execution, correlationKey);
		variables.setLong(ExecutionVariables.uploadedBytes.correlatedValue(correlationKey), 0L);
		variables.setVariable(ExecutionVariables.uploadedDuration.correlatedValue(correlationKey),
				new DurationValueImpl(Duration.ZERO));
		logger.debug("Saved error when trying to send ping message. Error message: {}", error.concept().getDisplay());
	}
}
