package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.service.AbstractService;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;

public class SetEndpointIdentifier extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(SetEndpointIdentifier.class);

	public SetEndpointIdentifier(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecuteWithErrorHandling(DelegateExecution execution, Variables variables) throws BpmnError
	{
		logger.debug("Setting endpoint identifier...");

		Task task = variables.getStartTask();
		String endpointIdentifierValue = getEndpointIdentifierValue(task);
		variables.setString(ExecutionVariables.targetEndpointIdentifier.name(), endpointIdentifierValue);

		logger.debug("Set endpoint identifier to " + endpointIdentifierValue);
	}

	private String getEndpointIdentifierValue(Task task)
	{
		return api.getTaskHelper()
				.getFirstInputParameterValue(task, CodeSystem.DsfPing.URL,
						CodeSystem.DsfPing.Code.ENDPOINT_IDENTIFIER.getValue(), Reference.class)
				.map(Reference::getIdentifier).map(Identifier::getValue).get();
	}
}
