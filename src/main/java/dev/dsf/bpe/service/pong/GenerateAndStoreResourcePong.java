package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.service.GenerateAndStoreResource;
import dev.dsf.bpe.util.Process;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class GenerateAndStoreResourcePong extends AbstractServiceDelegate
{
	private final GenerateAndStoreResource delegate;

	public GenerateAndStoreResourcePong(ProcessPluginApi api, long maxUploadSizeBytes)
	{
		super(api);
		this.delegate = new GenerateAndStoreResource(api, maxUploadSizeBytes, Process.PONG);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		delegate.doExecute(delegateExecution, variables);
	}

}
