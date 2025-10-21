package dev.dsf.bpe.variables.process_errors;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.dsf.bpe.ProcessErrors;
import dev.dsf.bpe.variables.GenericPrimitiveTypeSerializer;

public class ProcessErrorsValueSerializer extends GenericPrimitiveTypeSerializer<ProcessErrors, ProcessErrorsValueImpl>
{
	private static final ProcessErrorsValueTypeImpl TYPE = new ProcessErrorsValueTypeImpl();

	public ProcessErrorsValueSerializer()
	{
		super(TYPE, new ObjectMapper(), ProcessErrorsValueImpl.class, ProcessErrors.class);
	}
}
