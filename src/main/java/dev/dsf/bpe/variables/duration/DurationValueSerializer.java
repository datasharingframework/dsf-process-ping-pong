package dev.dsf.bpe.variables.duration;

import java.time.Duration;

import org.camunda.bpm.engine.variable.type.PrimitiveValueType;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.dsf.bpe.variables.GenericPrimitiveTypeSerializer;

public class DurationValueSerializer extends GenericPrimitiveTypeSerializer<Duration, DurationValueImpl>
{
	private static final PrimitiveValueType DURATION_VALUE_TYPE = new DurationValueTypeImpl();

	public DurationValueSerializer(ObjectMapper objectMapperWithJavaTimeModule)
	{
		super(DURATION_VALUE_TYPE, objectMapperWithJavaTimeModule, DurationValueImpl.class, Duration.class);
	}
}
