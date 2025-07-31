package dev.dsf.bpe.service.pong;

import java.time.Duration;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class EstimateCleanupTimerDuration extends AbstractServiceDelegate
{
	public EstimateCleanupTimerDuration(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		PingPongLogger logger = new PingPongLogger(EstimateCleanupTimerDuration.class, variables.getStartTask());

		logger.debug("Estimating cleanup timer duration...");
		final long minTimerDurationMillis = 20000;
		long downloadedDurationMillis = Optional
				.ofNullable(variables.getLong(ExecutionVariables.DOWNLOADED_DURATION_MILLIS.getValue())).orElse(0L);
		long timerDurationMillis = downloadedDurationMillis > Long.MAX_VALUE / 10 - minTimerDurationMillis
				? Long.MAX_VALUE
				: downloadedDurationMillis * 10 + minTimerDurationMillis;

		String cleanUpTimerDuration = Duration.ofMillis(timerDurationMillis).toString();
		variables.setString("cleanupTimerDuration", cleanUpTimerDuration);

		logger.debug("Estimated cleanup timer duration as {}", cleanUpTimerDuration);
	}
}
