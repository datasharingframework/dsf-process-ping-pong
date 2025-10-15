package dev.dsf.bpe.variables.codesystem.dsfpingstatus;

import java.io.Serial;

import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;

import dev.dsf.bpe.CodeSystem;

public class CodeValueImpl extends PrimitiveTypeValueImpl<CodeSystem.DsfPingStatus.Code> implements CodeValue
{
	@Serial
	private static final long serialVersionUID = 1L;

	private static final PrimitiveValueType DURATION_VALUE_TYPE = new CodeValueTypeImpl();

	public CodeValueImpl(CodeSystem.DsfPingStatus.Code value)
	{
		super(value, DURATION_VALUE_TYPE);
	}

	public CodeValueImpl(CodeSystem.DsfPingStatus.Code value, PrimitiveValueType type)
	{
		super(value, type);
	}
}
