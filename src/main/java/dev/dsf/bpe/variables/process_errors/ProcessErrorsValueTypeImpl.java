package dev.dsf.bpe.variables.process_errors;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

import dev.dsf.bpe.ProcessErrors;

public class ProcessErrorsValueTypeImpl extends PrimitiveValueTypeImpl
{
	private static final Class<ProcessErrors> PROCESS_ERRORS_CLASS = ProcessErrors.class;

	public ProcessErrorsValueTypeImpl()
	{
		super(PROCESS_ERRORS_CLASS);
	}

	@Override
	public TypedValue createValue(Object value, Map<String, Object> valueInfo)
	{
		if (value instanceof ProcessErrors errors)
		{
			return new ProcessErrorsValueImpl(errors);
		}
		else
		{
			throw new IllegalArgumentException("Cannot create value of type " + PROCESS_ERRORS_CLASS.getSimpleName()
					+ " from type " + value.getClass().getSimpleName());
		}
	}
}
