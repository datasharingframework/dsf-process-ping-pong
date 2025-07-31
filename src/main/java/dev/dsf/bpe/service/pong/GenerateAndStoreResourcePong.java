package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.service.GenerateAndStoreResource;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class GenerateAndStoreResourcePong extends AbstractServiceDelegate
{
	private final GenerateAndStoreResource delegate;

	public GenerateAndStoreResourcePong(ProcessPluginApi api, long maxUploadSizeBytes)
	{
		super(api);
		this.delegate = new GenerateAndStoreResource(api, maxUploadSizeBytes, CodeSystem.DsfPingProcesses.Code.PONG);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		delegate.doExecute(delegateExecution, variables);
	}

}
