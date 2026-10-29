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

public class SetEndpointIdentifier implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SetEndpointIdentifier.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Setting endpoint identifier...");

		Task task = variables.getStartTask();
		String endpointIdentifierValue = getEndpointIdentifierValue(api, task);
		variables.setString(ExecutionVariables.targetEndpointIdentifier.name(), endpointIdentifierValue);

		logger.debug("Set endpoint identifier to " + endpointIdentifierValue);
	}

	private String getEndpointIdentifierValue(ProcessPluginApi api, Task task)
	{
		return api.getTaskHelper()
				.getFirstInputParameterValue(task, CodeSystem.DsfPing.URL,
						CodeSystem.DsfPing.Code.ENDPOINT_IDENTIFIER.getValue(), Reference.class)
				.map(Reference::getIdentifier).map(Identifier::getValue).get();
	}
}
