package dev.dsf.bpe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import dev.dsf.bpe.v1.ProcessPluginDefinition;

public class PingProcessPluginDefinitionTest
{
	@Test
	public void testResourceLoading() throws Exception
	{
		ProcessPluginDefinition definition = new PingProcessPluginDefinition();
		Map<String, List<String>> resourcesByProcessId = definition.getFhirResourcesByProcessId();

		var ping = resourcesByProcessId.get(ConstantsPing.PROCESS_NAME_FULL_PING);
		assertNotNull(ping);
		assertEquals(9, ping.stream().filter(this::exists).count());

		var pingAutostart = resourcesByProcessId.get(ConstantsPing.PROCESS_NAME_FULL_PING_AUTOSTART);
		assertNotNull(pingAutostart);
		assertEquals(7, pingAutostart.stream().filter(this::exists).count());

		var pong = resourcesByProcessId.get(ConstantsPing.PROCESS_NAME_FULL_PONG);
		assertNotNull(pong);
		assertEquals(7, pong.stream().filter(this::exists).count());
	}

	private boolean exists(String file)
	{
		return getClass().getClassLoader().getResourceAsStream(file) != null;
	}
}
