package dev.dsf.bpe.service.pong;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.constants.CodeSystems.BpmnMessage;
import dev.dsf.bpe.v1.variables.Variables;

public class SelectPongTarget extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectPongTarget.class);

	public SelectPongTarget(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Task task = variables.getStartTask();

		String correlationKey = api.getTaskHelper()
				.getFirstInputParameterStringValue(task, BpmnMessage.URL, BpmnMessage.Codes.CORRELATION_KEY).get();
		String targetOrganizationIdentifierValue = task.getRequester().getIdentifier().getValue();
		String targetEndpointIdentifierValue = variables
				.getString(ConstantsPing.BPMN_EXECUTION_VARIABLE_PONG_TARGET_ENDPOINT_IDENTIFIER);

		String targetEndpointAddress = api.getEndpointProvider().getEndpointAddress(targetEndpointIdentifierValue)
				.orElseThrow(() ->
				{
					logger.warn(
							"Pong response target (organization {}, endpoint {}) not found locally or not active, not sending pong",
							targetOrganizationIdentifierValue, targetEndpointIdentifierValue);
					return new BpmnError("target_not_allowed");
				});

		variables.setTarget(variables.createTarget(targetOrganizationIdentifierValue, targetEndpointIdentifierValue,
				targetEndpointAddress, correlationKey));
	}
}
