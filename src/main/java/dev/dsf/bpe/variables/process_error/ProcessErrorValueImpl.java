package dev.dsf.bpe.variables.process_error;

import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;

import dev.dsf.bpe.ProcessError;

public class ProcessErrorValueImpl extends PrimitiveTypeValueImpl<ProcessError> implements ProcessErrorValue
{
	private static final ProcessErrorValueTypeImpl PROCESS_ERROR_VALUE_TYPE = new ProcessErrorValueTypeImpl();

	public ProcessErrorValueImpl(ProcessError value)
	{
		super(value, PROCESS_ERROR_VALUE_TYPE);
	}

	public ProcessErrorValueImpl(ProcessError value, PrimitiveValueType type)
	{
		super(value, type);
	}
}
