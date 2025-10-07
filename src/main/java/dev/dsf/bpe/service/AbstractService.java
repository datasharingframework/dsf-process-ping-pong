package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public abstract class AbstractService extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractService.class);

	public AbstractService(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws Exception
	{
		try
		{
			doExecuteWithErrorHandling(execution, variables);
		}
		catch (Exception e)
		{
			handleException(execution, variables, e);
		}
	}

	abstract protected void doExecuteWithErrorHandling(DelegateExecution execution, Variables variables)
			throws Exception;

	protected void handleException(DelegateExecution execution, Variables variables, Exception exception)
			throws Exception
	{
		logger.error("Unexpected error while executing service", exception);
		throw exception;
	}
}
