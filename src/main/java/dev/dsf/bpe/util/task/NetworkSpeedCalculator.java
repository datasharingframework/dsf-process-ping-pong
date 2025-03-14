package dev.dsf.bpe.util.task;

import java.math.BigDecimal;
import java.math.RoundingMode;

import dev.dsf.bpe.ConstantsPing;

public class NetworkSpeedCalculator
{
	public static BigDecimal calculate(int bytes, long duration, String unit)
	{
		if (bytes == 0)
			return BigDecimal.ZERO;
		if (duration == 0)
			return BigDecimal.valueOf(Long.MAX_VALUE);

		BigDecimal seconds = BigDecimal.valueOf(duration).setScale(3, RoundingMode.HALF_UP)
				.divide(BigDecimal.valueOf(1000).setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP);

		return switch (unit)
		{
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_BITS_PER_SECOND ->
			{
				BigDecimal bits = new BigDecimal(bytes * 8L).setScale(3, RoundingMode.HALF_UP);
				yield bits.divide(seconds, 2, RoundingMode.HALF_UP);
			}
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABITS_PER_SECOND ->
			{
				BigDecimal megabits = new BigDecimal(bytes * 8L).divide(BigDecimal.valueOf(1000000),
						RoundingMode.HALF_UP);
				yield megabits.divide(seconds, 2, RoundingMode.HALF_UP);
			}
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_BYTES_PER_SECOND ->
			{
				BigDecimal bytesLocal = new BigDecimal(bytes).setScale(3, RoundingMode.HALF_UP);
				yield bytesLocal.divide(seconds, 2, RoundingMode.HALF_UP);
			}
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABYTES_PER_SECOND ->
			{
				BigDecimal megabytes = new BigDecimal(bytes).divide(BigDecimal.valueOf(1000000), RoundingMode.HALF_UP);
				yield megabytes.divide(seconds, 2, RoundingMode.HALF_UP);
			}
			default -> BigDecimal.ZERO;
		};
	}
}
