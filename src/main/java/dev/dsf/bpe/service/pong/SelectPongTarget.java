package dev.dsf.bpe.service.pong;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.constants.CodeSystems.BpmnMessage;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class SelectPongTarget implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SelectPongTarget.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Selecting pong targets...");

		Task task = variables.getStartTask();

		String correlationKey = api.getTaskHelper()
				.getFirstInputParameterStringValue(task, BpmnMessage.SYSTEM, BpmnMessage.Codes.CORRELATION_KEY).get();
		String targetOrganizationIdentifierValue = task.getRequester().getIdentifier().getValue();
		String targetEndpointIdentifierValue = variables.getString(ExecutionVariables.targetEndpointIdentifier.name());

		String targetEndpointAddress = api.getEndpointProvider().getEndpointAddress(targetEndpointIdentifierValue)
				.orElseThrow(() ->
				{
					logger.warn(
							"Pong response target (organization {}, endpoint {}) not found locally or not active, not sending pong",
							targetOrganizationIdentifierValue, targetEndpointIdentifierValue);
					return new ErrorBoundaryEvent("target_not_allowed", null );
				});

		variables.setTarget(variables.createTarget(targetOrganizationIdentifierValue, targetEndpointIdentifierValue,
				targetEndpointAddress, correlationKey));
		logger.debug("Selected pong targets.");
	}
}
