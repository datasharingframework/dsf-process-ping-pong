package dev.dsf.bpe.service;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.ConstantsBase;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.delegate.AbstractServiceDelegate;
import dev.dsf.fhir.authorization.read.ReadAccessHelper;
import dev.dsf.fhir.client.FhirWebserviceClientProvider;
import dev.dsf.fhir.organization.EndpointProvider;
import dev.dsf.fhir.task.TaskHelper;
import dev.dsf.fhir.variables.Target;
import dev.dsf.fhir.variables.TargetValues;

public class SelectPongTarget extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(SelectPongTarget.class);

	private final EndpointProvider endpointProvider;

	public SelectPongTarget(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, EndpointProvider endpointProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.endpointProvider = endpointProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(endpointProvider, "endpointProvider");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables(execution);

		String correlationKey = getTaskHelper().getFirstInputParameterStringValue(task,
				ConstantsBase.CODESYSTEM_DSF_BPMN, ConstantsBase.CODESYSTEM_DSF_BPMN_VALUE_CORRELATION_KEY).get();
		String targetOrganizationIdentifierValue = task.getRequester().getIdentifier().getValue();
		String targetEndpointIdentifierValue = getEndpointIdentifierValue(task);

		String targetEndpointAddress = endpointProvider.getEndpointAddress(targetEndpointIdentifierValue)
				.orElseThrow(() ->
				{
					logger.warn(
							"Pong response target (organization {}, endpoint {}) not found locally or not active, not sending pong",
							targetOrganizationIdentifierValue, targetEndpointIdentifierValue);
					return new BpmnError("target_not_allowed");
				});

		execution.setVariable(ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET,
				TargetValues.create(Target.createBiDirectionalTarget(targetOrganizationIdentifierValue,
						targetEndpointIdentifierValue, targetEndpointAddress, correlationKey)));
	}

	private String getEndpointIdentifierValue(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterReferenceValue(task, ConstantsPing.CODESYSTEM_DSF_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_VALUE_ENDPOINT_IDENTIFIER)
				.map(Reference::getIdentifier).map(Identifier::getValue).get();
	}
}
