package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.service.Cleanup;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class CleanupPong extends AbstractServiceDelegate
{
	private final Cleanup delegate;

	public CleanupPong(ProcessPluginApi api)
	{
		super(api);
		delegate = new Cleanup(api, CodeSystem.DsfPingProcesses.Code.PONG);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		delegate.doExecute(delegateExecution, variables);
	}
}
