package dev.dsf.bpe.util.task;

import java.math.BigDecimal;
import java.math.RoundingMode;

import dev.dsf.bpe.ConstantsPing;

public class NetworkSpeedCalculator
{
	public static BigDecimal calculate(int bytes, long duration, String unit)
	{
		return switch (unit)
		{
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_BITS_PER_SECOND ->
				new BigDecimal(bytes * 8L).divide(BigDecimal.valueOf(duration / 1000), RoundingMode.HALF_DOWN);
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABITS_PER_SECOND ->
				new BigDecimal(bytes * 8L).divide(BigDecimal.valueOf(1000000), RoundingMode.UNNECESSARY)
						.divide(BigDecimal.valueOf(duration / 1000), RoundingMode.HALF_DOWN);
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_BYTES_PER_SECOND ->
				new BigDecimal(bytes).divide(BigDecimal.valueOf(duration / 1000), RoundingMode.HALF_DOWN);
			case ConstantsPing.CODESYSTEM_DSF_PING_UNITS_VALUE_MEGABYTES_PER_SECOND ->
				new BigDecimal(bytes).divide(BigDecimal.valueOf(1000000), RoundingMode.UNNECESSARY)
						.divide(BigDecimal.valueOf(duration / 1000), RoundingMode.HALF_DOWN);
			default -> new BigDecimal(0);
		};
	}
}
