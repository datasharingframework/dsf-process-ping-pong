package dev.dsf.bpe.service;

import java.time.Duration;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
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
		long downloadedDurationMillis = variables.getLong(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOADED_DURATION_MILLIS);
		long timerDurationMillis = downloadedDurationMillis > Long.MAX_VALUE / 10 ? Long.MAX_VALUE : downloadedDurationMillis * 10;

		String cleanUpTimerDuration = Duration.ofMillis(timerDurationMillis).toString();
		variables.setString("cleanupTimerDuration", cleanUpTimerDuration);

		logger.info("Estimated cleanup timer duration");
	}
}
