package dev.dsf.bpe.variables.codesystem.dsfpingstatus;

import org.camunda.bpm.engine.variable.type.PrimitiveValueType;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.variables.GenericPrimitiveTypeSerializer;

public class CodeValueSerializer extends GenericPrimitiveTypeSerializer<CodeSystem.DsfPingStatus.Code, CodeValueImpl>
{
	private static final PrimitiveValueType DURATION_VALUE_TYPE = new CodeValueTypeImpl();

	public CodeValueSerializer()
	{
		super(DURATION_VALUE_TYPE, new ObjectMapper(), CodeValueImpl.class, CodeSystem.DsfPingStatus.Code.class);
	}
}
