package dev.dsf.bpe.variables;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class GenericPrimitiveTypeSerializer<E, T extends PrimitiveTypeValueImpl<E> & PrimitiveValue<E>>
		extends PrimitiveValueSerializer<T> implements InitializingBean
{
	private final ObjectMapper objectMapper;
	private final Class<T> typeClass;
	private final Class<E> valueClass;
	private final PrimitiveValueType primitiveValueType;

	public GenericPrimitiveTypeSerializer(PrimitiveValueType variableType, ObjectMapper objectMapper,
			Class<T> typeClass, Class<E> valueClass)
	{
		super(variableType);
		this.objectMapper = objectMapper;
		this.typeClass = typeClass;
		this.valueClass = valueClass;
		this.primitiveValueType = variableType;
	}

	@Override
	public void afterPropertiesSet()
	{
		Objects.requireNonNull(objectMapper);
	}

	@Override
	public T readValue(ValueFields valueFields, boolean asTransientValue)
	{
		try
		{
			byte[] bytes = valueFields.getByteArrayValue();
			return (bytes == null || bytes.length == 0) ? null
					: typeClass.getConstructor(valueClass, PrimitiveValueType.class)
							.newInstance(objectMapper.readValue(bytes, valueClass), primitiveValueType);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeValue(T value, ValueFields valueFields)
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
	public T convertToTypedValue(UntypedValueImpl untypedValue)
	{
		try
		{
			if (untypedValue != null && typeClass.isAssignableFrom(untypedValue.getValue().getClass()))
			{
				T typedValue = typeClass.cast(untypedValue.getValue());

				return typeClass.getConstructor(typeClass, primitiveValueType.getClass()).newInstance(typedValue,
						primitiveValueType);

			}
			else if (untypedValue != null)
			{
				throw new IllegalArgumentException("Cannot convert "
						+ untypedValue.getValue().getClass().getSimpleName() + " to " + typeClass.getSimpleName());
			}
			throw new IllegalArgumentException("Cannot convert " + null + " to " + typeClass.getSimpleName());
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException("Cannot convert " + untypedValue.getValue().getClass().getSimpleName()
					+ " to " + typeClass.getSimpleName());
		}
		catch (InvocationTargetException | IllegalAccessException | InstantiationException e)
		{
			throw new RuntimeException(e);
		}
	}
}
