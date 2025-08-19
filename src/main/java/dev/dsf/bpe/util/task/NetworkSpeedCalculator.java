package dev.dsf.bpe.util.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

import dev.dsf.bpe.CodeSystem;

public final class NetworkSpeedCalculator
{
	private NetworkSpeedCalculator()
	{
	}

	public static BigDecimal calculate(long bytes, Duration duration, CodeSystem.DsfPingUnits.Code unit)
	{
		if (bytes == 0)
			return BigDecimal.ZERO;
		if (duration.isZero())
			return BigDecimal.valueOf(Long.MAX_VALUE);

		return unit.calculateSpeed(bytes, duration);
	}
}
