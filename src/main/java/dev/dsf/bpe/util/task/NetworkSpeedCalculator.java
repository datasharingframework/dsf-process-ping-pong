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

		BigDecimal seconds = BigDecimal.valueOf(duration.toMillis()).setScale(3, RoundingMode.HALF_UP)
				.divide(BigDecimal.valueOf(1000).setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP);

		return switch (unit) // todo: unit can implement calculate for each entry
		{
			case BITS_PER_SECOND ->
			{
				BigDecimal bits = new BigDecimal(bytes * 8L).setScale(3, RoundingMode.HALF_UP);
				yield bits.divide(seconds, 2, RoundingMode.HALF_UP);
			}
			case MEGABITS_PER_SECOND ->
			{
				BigDecimal megabits = new BigDecimal(bytes * 8L).divide(BigDecimal.valueOf(1000000),
						RoundingMode.HALF_UP);
				yield megabits.divide(seconds, 2, RoundingMode.HALF_UP);
			}
			case BYTES_PER_SECOND ->
			{
				BigDecimal bytesLocal = new BigDecimal(bytes).setScale(3, RoundingMode.HALF_UP);
				yield bytesLocal.divide(seconds, 2, RoundingMode.HALF_UP);
			}
			case MEGABYTES_PER_SECOND ->
			{
				BigDecimal megabytes = new BigDecimal(bytes).divide(BigDecimal.valueOf(1000000), RoundingMode.HALF_UP);
				yield megabytes.divide(seconds, 2, RoundingMode.HALF_UP);
			}
		};
	}
}
