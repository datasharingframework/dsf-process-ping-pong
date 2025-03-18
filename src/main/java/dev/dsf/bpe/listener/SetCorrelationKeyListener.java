package dev.dsf.bpe.listener;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.constants.BpmnExecutionVariables;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class SetCorrelationKeyListener implements ExecutionListener, InitializingBean
{
	private final ProcessPluginApi api;

	public SetCorrelationKeyListener(ProcessPluginApi api)
	{
		this.api = api;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(api, "api");
	}

	@Override
	public void notify(DelegateExecution execution) throws Exception
	{
		PingPongLogger logger = new PingPongLogger(SetCorrelationKeyListener.class,
				api.getVariables(execution).getStartTask());
		logger.debug("Setting correlation key for subprocess instance {}", execution.getProcessInstanceId());
		Variables variables = api.getVariables(execution);
		Target target = variables.getTarget();

		execution.setVariableLocal(BpmnExecutionVariables.CORRELATION_KEY, target.getCorrelationKey());
	}
}
