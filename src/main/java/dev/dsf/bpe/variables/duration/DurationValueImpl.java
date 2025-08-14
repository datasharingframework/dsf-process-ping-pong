package dev.dsf.bpe.variables.duration;

import java.time.Duration;

import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;

public class DurationValueImpl extends PrimitiveTypeValueImpl<Duration> implements DurationValue
{
	private static final PrimitiveValueType DURATION_VALUE_TYPE = new DurationValueTypeImpl();

	public DurationValueImpl(Duration value)
	{
		super(value, DURATION_VALUE_TYPE);
	}

	public DurationValueImpl(Duration value, PrimitiveValueType type)
	{
		super(value, type);
	}
}
