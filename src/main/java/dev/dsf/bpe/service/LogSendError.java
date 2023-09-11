package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class LogSendError extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogSendError.class);

	public LogSendError(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Target target = variables.getTarget();
		String statusCode = (String) execution.getVariableLocal("statusCode");
		String errorMessage = (String) execution.getVariableLocal("errorMessage");

		logger.warn("Unable to send PING to {} (endpoint: {}): {}", target.getOrganizationIdentifierValue(),
				target.getEndpointIdentifierValue(), errorMessage);

		variables.setString("statusCode_" + target.getCorrelationKey(), statusCode);
		variables.setString("errorMessage_" + target.getCorrelationKey(), errorMessage);
	}
}
