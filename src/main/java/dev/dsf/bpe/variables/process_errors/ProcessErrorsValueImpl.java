package dev.dsf.bpe.variables.process_errors;

import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;

import dev.dsf.bpe.ProcessErrors;

public class ProcessErrorsValueImpl extends PrimitiveTypeValueImpl<ProcessErrors> implements ProcessErrorsValue
{
	private static final ProcessErrorsValueTypeImpl PROCESS_ERRORS_VALUE_TYPE = new ProcessErrorsValueTypeImpl();

	public ProcessErrorsValueImpl(ProcessErrors value)
	{
		super(value, PROCESS_ERRORS_VALUE_TYPE);
	}

	public ProcessErrorsValueImpl(ProcessErrors value, PrimitiveValueType type)
	{
		super(value, type);
	}
}
