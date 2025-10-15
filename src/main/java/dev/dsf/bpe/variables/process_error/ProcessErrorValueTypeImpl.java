package dev.dsf.bpe.variables.process_error;

import java.io.Serial;
import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

import dev.dsf.bpe.ProcessError;

public class ProcessErrorValueTypeImpl extends PrimitiveValueTypeImpl
{
	@Serial
	private static final long serialVersionUID = 1L;

	private static final Class<ProcessError> PROCESS_ERROR_CLASS = ProcessError.class;

	public ProcessErrorValueTypeImpl()
	{
		super(PROCESS_ERROR_CLASS);
	}

	@Override
	public TypedValue createValue(Object value, Map<String, Object> valueInfo)
	{
		if (value instanceof ProcessError error)
		{
			return new ProcessErrorValueImpl(error);
		}
		else
		{
			throw new IllegalArgumentException("Cannot create value of type " + PROCESS_ERROR_CLASS.getSimpleName()
					+ " from type " + value.getClass().getSimpleName());
		}
	}
}
