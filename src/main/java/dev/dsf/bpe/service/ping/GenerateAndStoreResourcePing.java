package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.service.GenerateAndStoreResource;
import dev.dsf.bpe.util.Process;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class GenerateAndStoreResourcePing extends AbstractServiceDelegate
{
	private final GenerateAndStoreResource delegate;

	public GenerateAndStoreResourcePing(ProcessPluginApi api, long maxUploadSizeBytes)
	{
		super(api);
		delegate = new GenerateAndStoreResource(api, maxUploadSizeBytes, Process.PING);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		delegate.doExecute(delegateExecution, variables);
	}
}
