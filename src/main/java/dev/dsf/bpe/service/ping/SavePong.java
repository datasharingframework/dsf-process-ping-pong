package dev.dsf.bpe.service.ping;

import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.util.ErrorMessageListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;

public class SavePong extends AbstractServiceDelegate
{
	public SavePong(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception
	{
		PingPongLogger logger = new PingPongLogger(SavePong.class, variables.getStartTask());

		Target target = variables.getTarget();
		logger.debug("Pong received from {}. Saving pong information...", target.getEndpointUrl());
		String correlationKey = target.getCorrelationKey();
		delegateExecution.removeVariable("statusCode");
		variables.setString(ConstantsPing.getBpmnExecutionVariableStatusCode(correlationKey),
				ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_RECEIVED);

		Task pong = variables.getLatestTask();

		long downloadedDurationMillis = api.getTaskHelper()
				.getFirstInputParameterValue(pong, ConstantsPing.CODESYSTEM_DSF_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_DURATION_MILLIS, DecimalType.class)
				.get().getValue().longValue();
		variables.setLong(ConstantsPing.getBpmnExecutionVariableUploadedDurationMillis(correlationKey),
				downloadedDurationMillis);

		int downloadedBytes = api.getTaskHelper().getFirstInputParameterValue(pong, ConstantsPing.CODESYSTEM_DSF_PING,
				ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_BYTES, IntegerType.class).get().getValue();
		variables.setInteger(ConstantsPing.getBpmnExecutionVariableUploadedBytes(correlationKey), downloadedBytes);

		List<String> errorList = api.getTaskHelper()
				.getInputParameterValues(pong, ConstantsPing.CODESYSTEM_DSF_PING,
						ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_ERROR_MESSAGE, StringType.class)
				.map(PrimitiveType::getValue).toList();
		ErrorMessageListUtils.addAll(errorList, delegateExecution);

		logger.debug("Saved pong information.");
	}
}
