package dev.dsf.bpe.service;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.delegate.AbstractServiceDelegate;
import dev.dsf.fhir.authorization.read.ReadAccessHelper;
import dev.dsf.fhir.client.FhirWebserviceClientProvider;
import dev.dsf.fhir.task.TaskHelper;

public class LogPing extends AbstractServiceDelegate
{
	private static final Logger logger = LoggerFactory.getLogger(LogPing.class);

	public LogPing(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables(execution);

		logger.info("PING from {} (endpoint: {})", task.getRequester().getIdentifier().getValue(),
				getEndpointIdentifierValue(task));
	}

	private String getEndpointIdentifierValue(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterReferenceValue(task, ConstantsPing.CODESYSTEM_DSF_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_VALUE_ENDPOINT_IDENTIFIER)
				.map(Reference::getIdentifier).map(Identifier::getValue).get();
	}
}
