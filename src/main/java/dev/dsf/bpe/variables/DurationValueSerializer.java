package dev.dsf.bpe.variables;

import java.time.Duration;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DurationValueSerializer extends PrimitiveValueSerializer<DurationValue> implements InitializingBean
{
	private static final PrimitiveValueType DURATION_VALUE_TYPE = new DurationValueTypeImpl();
	private final ObjectMapper objectMapper;

	public DurationValueSerializer()
	{
		super(DURATION_VALUE_TYPE);
		this.objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
	}

	@Override
	public void afterPropertiesSet()
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(DurationValue value, ValueFields valueFields)
	{
		try
		{
			valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(value.getValue()));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public DurationValueImpl readValue(ValueFields valueFields, boolean asTransientValue)
	{
		try
		{
			byte[] bytes = valueFields.getByteArrayValue();
			return (bytes == null || bytes.length == 0) ? null
					: new DurationValueImpl(objectMapper.readValue(bytes, Duration.class));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public DurationValueImpl convertToTypedValue(UntypedValueImpl untypedValue)
	{
		if (untypedValue != null && untypedValue.getValue() instanceof Duration duration)
		{
			return new DurationValueImpl(duration);
		}
		else if (untypedValue != null)
		{
			throw new IllegalArgumentException(
					"Cannot convert " + untypedValue.getValue().getClass().getSimpleName() + " to DurationValueImpl");
		}
		throw new IllegalArgumentException("Cannot convert " + untypedValue + " to DurationValueImpl");
	}
}
