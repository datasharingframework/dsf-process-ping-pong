package dev.dsf.bpe.service.pong;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class LogPingFirstTry implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(LogPingFirstTry.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		Task task = variables.getLatestTask();
		String pingTry = "1";

		variables.setString(ExecutionVariables.pingTry.name(), pingTry);
		logger.info("PING from {} (endpoint: {}, try: {})", task.getRequester().getIdentifier().getValue(),
				getEndpointIdentifierValue(api, task), pingTry);
	}

	private String getEndpointIdentifierValue(ProcessPluginApi api, Task task)
	{
		return api.getTaskHelper()
				.getFirstInputParameterValue(task, CodeSystem.DsfPing.URL,
						CodeSystem.DsfPing.Code.ENDPOINT_IDENTIFIER.getValue(), Reference.class)
				.map(Reference::getIdentifier).map(Identifier::getValue).get();
	}
}
