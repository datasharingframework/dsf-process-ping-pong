package dev.dsf.bpe.util.task;

import java.math.BigDecimal;
import java.math.MathContext;

import dev.dsf.bpe.ConstantsPing;

public class NetworkSpeedCalculator
{
	public static BigDecimal calculate(int bytes, long duration, String unit)
	{
		if (bytes == 0) return BigDecimal.ZERO;
		if (duration == 0) return BigDecimal.valueOf(Long.MAX_VALUE);

		MathContext mathContext = new MathContext(4);
		return switch (unit)
		{
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_BITS_PER_SECOND -> new BigDecimal(bytes * 8L)
					.divide(BigDecimal.valueOf(duration).divide(BigDecimal.valueOf(1000), mathContext), mathContext);
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABITS_PER_SECOND ->
				new BigDecimal(bytes * 8L).divide(BigDecimal.valueOf(1000000), mathContext).divide(
						BigDecimal.valueOf(duration).divide(BigDecimal.valueOf(1000), mathContext), mathContext);
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_BYTES_PER_SECOND -> new BigDecimal(bytes)
					.divide(BigDecimal.valueOf(duration).divide(BigDecimal.valueOf(1000), mathContext), mathContext);
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABYTES_PER_SECOND ->
				new BigDecimal(bytes).divide(BigDecimal.valueOf(1000000), mathContext).divide(
						BigDecimal.valueOf(duration).divide(BigDecimal.valueOf(1000), mathContext), mathContext);
			default -> BigDecimal.ZERO;
		};
	}
}
