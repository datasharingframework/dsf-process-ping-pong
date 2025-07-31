package dev.dsf.bpe.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import dev.dsf.bpe.CodeSystem;

public class CodeSystemDsfPingUnitsConverter implements Converter<String, CodeSystem.DsfPingUnits.Code>
{
	@Override
	public CodeSystem.DsfPingUnits.Code convert(@NonNull String source)
	{
		return CodeSystem.DsfPingUnits.Code.ofValue(source);
	}
}
