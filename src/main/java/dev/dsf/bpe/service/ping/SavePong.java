package dev.dsf.bpe.service.ping;

import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.util.logging.PingPongLogger;
import dev.dsf.bpe.util.task.input.ErrorInputParser;
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

		Task pong = variables.getLatestTask();

		Optional<DecimalType> optDownloadedDurationMillis = api.getTaskHelper().getFirstInputParameterValue(pong,
				ConstantsPing.CODESYSTEM_DSF_PING, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_DURATION_MILLIS,
				DecimalType.class);
		optDownloadedDurationMillis.ifPresent(decimalType -> variables.setLong(
				ConstantsPing.getBpmnExecutionVariableUploadedDurationMillis(correlationKey),
				decimalType.getValue().longValue()));

		Optional<DecimalType> optDownloadedBytes = api.getTaskHelper().getFirstInputParameterValue(pong,
				ConstantsPing.CODESYSTEM_DSF_PING, ConstantsPing.CODESYSTEM_DSF_PING_VALUE_DOWNLOADED_BYTES,
				DecimalType.class);
		optDownloadedBytes.ifPresent(
				decimalType -> variables.setLong(ConstantsPing.getBpmnExecutionVariableUploadedBytes(correlationKey),
						decimalType.getValue().longValue()));


		List<ProcessError> errorList = ErrorInputParser.parseInputs(pong);

		ErrorListUtils.addAll(errorList, delegateExecution, correlationKey);

		logger.debug("Saved pong information.");
	}
}
