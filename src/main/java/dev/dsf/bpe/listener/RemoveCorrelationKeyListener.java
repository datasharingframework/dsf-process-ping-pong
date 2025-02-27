package dev.dsf.bpe.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class RemoveCorrelationKeyListener implements ExecutionListener
{
	private final ProcessPluginApi api;

	public RemoveCorrelationKeyListener(ProcessPluginApi api)
	{
		this.api = api;
	}

	@Override
	public void notify(DelegateExecution delegateExecution) throws Exception
	{
		Variables variables = api.getVariables(delegateExecution);
		Target oldTarget = variables.getTarget();
		variables.setTarget(variables.createTarget(oldTarget.getOrganizationIdentifierValue(),
				oldTarget.getEndpointIdentifierValue(), oldTarget.getEndpointUrl()));
	}
}
