package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class EstimateCleanupTimerDuration extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(EstimateCleanupTimerDuration.class);

	public EstimateCleanupTimerDuration(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		logger.info("Estimated cleanup timer duration");
		variables.setString("cleanupTimerDuration", "PT20S");
	}
}
