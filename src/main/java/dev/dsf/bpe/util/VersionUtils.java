package dev.dsf.bpe.util;

import dev.dsf.bpe.PingProcessPluginDefinition;

public class VersionUtils
{
	public static String appendFhirResourceVersion(String toAppend)
	{
		return toAppend + "|" + getFhirResourceVersion();
	}

	public static String getFhirResourceVersion()
	{
		return new PingProcessPluginDefinition().getVersion().substring(0, 3);
	}
}
