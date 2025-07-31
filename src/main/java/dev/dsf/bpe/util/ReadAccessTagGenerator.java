package dev.dsf.bpe.util;

import org.hl7.fhir.r4.model.Coding;

import dev.dsf.bpe.CodeSystem;

public class ReadAccessTagGenerator
{
	public static Coding create(String accessLevel)
	{
		Coding tag = new Coding();
		tag.setSystem(CodeSystem.ReadAccessTag.URL);
		tag.setCode(accessLevel);
		return tag;
	}
}
