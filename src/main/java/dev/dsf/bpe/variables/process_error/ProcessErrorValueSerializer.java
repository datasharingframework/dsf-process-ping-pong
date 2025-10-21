package dev.dsf.bpe.variables.process_error;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.variables.GenericPrimitiveTypeSerializer;

public class ProcessErrorValueSerializer extends GenericPrimitiveTypeSerializer<ProcessError, ProcessErrorValueImpl>
{
	private static final ProcessErrorValueTypeImpl PROCESS_ERROR_VALUE_TYPE = new ProcessErrorValueTypeImpl();

	public ProcessErrorValueSerializer()
	{
		super(PROCESS_ERROR_VALUE_TYPE, new ObjectMapper(), ProcessErrorValueImpl.class, ProcessError.class);
	}
}
