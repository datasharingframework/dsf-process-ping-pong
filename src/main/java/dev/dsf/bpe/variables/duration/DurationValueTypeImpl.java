package dev.dsf.bpe.variables.duration;

import java.time.Duration;
import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;

public class DurationValueTypeImpl extends PrimitiveValueTypeImpl
{
	private static final Class<Duration> DURATION_CLASS = Duration.class;

	public DurationValueTypeImpl()
	{
		super(DURATION_CLASS);
	}

	@Override
	public DurationValueImpl createValue(Object o, Map<String, Object> map)
	{
		if (o instanceof Duration duration)
		{
			return new DurationValueImpl(duration);
		}
		else
		{
			throw new IllegalArgumentException("Cannot create value of type " + DURATION_CLASS.getSimpleName()
					+ " from type " + o.getClass().getSimpleName());
		}
	}
}
