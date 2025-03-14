package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class LogAndSaveNoResponse extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogAndSaveNoResponse.class);

	public LogAndSaveNoResponse(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		Target target = variables.getTarget();
		logger.info("No PONG received from endpoint '{}'", target.getEndpointIdentifierValue());

		String correlationKey = target.getCorrelationKey();
		delegateExecution.removeVariable("statusCode");
		variables.setString(ConstantsPing.getBpmnExecutionVariableStatusCode(correlationKey),
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_MISSING);
	}
}
