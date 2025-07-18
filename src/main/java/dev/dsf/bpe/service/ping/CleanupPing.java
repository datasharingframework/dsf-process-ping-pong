package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.service.Cleanup;
import dev.dsf.bpe.util.Process;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class CleanupPing extends AbstractServiceDelegate
{
	private final Cleanup delegate;

	public CleanupPing(ProcessPluginApi api)
	{
		super(api);
		delegate = new Cleanup(api, Process.PING);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		delegate.doExecute(delegateExecution, variables);
	}
}
