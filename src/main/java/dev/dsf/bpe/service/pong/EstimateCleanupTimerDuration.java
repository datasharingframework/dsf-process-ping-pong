package dev.dsf.bpe.service.pong;

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class EstimateCleanupTimerDuration implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(EstimateCleanupTimerDuration.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Estimating cleanup timer duration...");
		final long minTimerDurationMillis = 20000;
		Duration downloadedDuration = Optional
				.ofNullable((Duration) variables.getVariable(ExecutionVariables.downloadedDuration.name()))
				.orElse(Duration.ZERO);
		long timerDurationMillis = downloadedDuration.toMillis() > Long.MAX_VALUE / 10 - minTimerDurationMillis
				? Long.MAX_VALUE
				: downloadedDuration.toMillis() * 10 + minTimerDurationMillis;

		String cleanUpTimerDuration = Duration.ofMillis(timerDurationMillis).toString();
		variables.setString(ExecutionVariables.cleanupTimerDuration.name(), cleanUpTimerDuration);

		logger.debug("Estimated cleanup timer duration as {}", cleanUpTimerDuration);
	}
}
