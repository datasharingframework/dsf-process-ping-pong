package dev.dsf.bpe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.fhir.resources.ResourceProvider;

public class PingProcessPluginDefinitionTest
{
	@Test
	public void testResourceLoading() throws Exception
	{
		ProcessPluginDefinition definition = new PingProcessPluginDefinition();
		ResourceProvider provider = definition.getResourceProvider(FhirContext.forR4(), getClass().getClassLoader(),
				new StandardEnvironment());
		assertNotNull(provider);

		var ping = provider
				.getResources(ConstantsPing.PROCESS_NAME_FULL_PING + "/" + PingProcessPluginDefinition.VERSION);
		assertNotNull(ping);
		assertEquals(8, ping.count());

		var pingAutostart = provider.getResources(
				ConstantsPing.PROCESS_NAME_FULL_PING_AUTOSTART + "/" + PingProcessPluginDefinition.VERSION);
		assertNotNull(pingAutostart);
		assertEquals(5, pingAutostart.count());

		var pong = provider
				.getResources(ConstantsPing.PROCESS_NAME_FULL_PONG + "/" + PingProcessPluginDefinition.VERSION);
		assertNotNull(pong);
		assertEquals(7, pong.count());
	}
}
