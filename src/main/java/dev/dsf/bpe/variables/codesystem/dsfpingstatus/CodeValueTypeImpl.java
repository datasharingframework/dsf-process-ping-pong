package dev.dsf.bpe.variables.codesystem.dsfpingstatus;

import java.io.Serial;
import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;

import dev.dsf.bpe.CodeSystem;

public class CodeValueTypeImpl extends PrimitiveValueTypeImpl
{
	@Serial
	private static final long serialVersionUID = 1L;

	private static final Class<CodeSystem.DsfPingStatus.Code> CODE_CLASS = CodeSystem.DsfPingStatus.Code.class;

	public CodeValueTypeImpl()
	{
		super(CODE_CLASS);
	}

	@Override
	public CodeValueImpl createValue(Object o, Map<String, Object> map)
	{
		if (o instanceof CodeSystem.DsfPingStatus.Code code)
		{
			return new CodeValueImpl(code);
		}
		else
		{
			throw new IllegalArgumentException("Cannot create value of type " + CODE_CLASS.getSimpleName()
					+ " from type " + o.getClass().getSimpleName());
		}
	}
}
