package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;

public class LogPing extends AbstractServiceDelegate
{
	public LogPing(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(LogPing.class, variables.getStartTask());

		Task task = variables.getLatestTask();

		logger.info("PING from {} (endpoint: {})", task.getRequester().getIdentifier().getValue(),
				getEndpointIdentifierValue(task));
	}

	private String getEndpointIdentifierValue(Task task)
	{
		return api.getTaskHelper()
				.getFirstInputParameterValue(task, CodeSystem.DsfPing.URL,
						CodeSystem.DsfPing.Code.ENDPOINT_IDENTIFIER.getValue(), Reference.class)
				.map(Reference::getIdentifier).map(Identifier::getValue).get();
	}
}
