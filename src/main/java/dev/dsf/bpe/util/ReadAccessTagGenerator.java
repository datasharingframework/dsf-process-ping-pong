package dev.dsf.bpe.util;

import org.hl7.fhir.r4.model.Coding;

import dev.dsf.bpe.ConstantsPing;

public class ReadAccessTagGenerator
{
	public static Coding create(String accessLevel)
	{
		Coding tag = new Coding();
		tag.setSystem(ConstantsPing.CODESYSTEM_READ_ACCESS_TAG);
		tag.setCode(accessLevel);
		return tag;
	}
}
