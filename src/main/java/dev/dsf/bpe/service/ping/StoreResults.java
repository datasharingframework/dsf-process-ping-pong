package dev.dsf.bpe.service.ping;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.mail.ErrorMailService;
import dev.dsf.bpe.util.ErrorMessageListUtils;
import dev.dsf.bpe.util.task.NetworkSpeedCalculator;
import dev.dsf.bpe.util.task.output.generator.PingStatusGenerator;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Targets;
import dev.dsf.bpe.v1.variables.Variables;

public class StoreResults extends AbstractServiceDelegate implements InitializingBean
{
	private final ErrorMailService errorMailService;
	private final String networkSpeedUnit;

	public StoreResults(ProcessPluginApi api, ErrorMailService errorMailService, String networkSpeedUnit)
	{
		super(api);
		this.networkSpeedUnit = networkSpeedUnit;
		this.errorMailService = errorMailService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(errorMailService, "errorMailService");
	}

	@Override
	protected void doExecute(DelegateExecution execution, Variables variables) throws BpmnError, Exception
	{
		Task task = variables.getStartTask();
		Targets targets = variables.getTargets();

		targets.getEntries().stream().sorted(Comparator.comparing(Target::getEndpointIdentifierValue)).forEach(target ->
		{
			String correlationKey = target.getCorrelationKey();

			String statusCode = variables.getString(ConstantsPing.getBpmnExecutionVariableStatusCode(correlationKey));
			if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE.equals(statusCode)
					|| ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED.equals(statusCode))
			{
				List<String> errorMessages = ErrorMessageListUtils.getErrorMessageList(execution);
				String errorMessage = errorMessages.get(0);
				task.addOutput(PingStatusGenerator.createPingStatusOutput(target, statusCode, errorMessages));

				if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_REACHABLE.equals(statusCode))
					errorMailService.endpointNotReachableForPing(task.getIdElement(), target, errorMessage);
				else if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_NOT_ALLOWED.equals(statusCode))
					errorMailService.endpointReachablePingForbidden(task.getIdElement(), target, errorMessage);
			}
			else if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_MISSING.equals(statusCode))
			{
				List<String> errorMessages = ErrorMessageListUtils.getErrorMessageList(execution);
				task.addOutput(PingStatusGenerator.createPingStatusOutput(target, statusCode, errorMessages));
			}
			else
			{
				int downloadResourceSizeBytes = variables
						.getInteger(ConstantsPing.BPMN_EXECUTION_VARIABLE_DOWNLOAD_RESOURCE_SIZE_BYTES);
				List<String> errorMessageList = ErrorMessageListUtils.getErrorMessageList(execution);
				if (downloadResourceSizeBytes >= 0)
				{
					int downloadedBytes = variables
							.getInteger(ConstantsPing.getBpmnExecutionVariableDownloadedBytes(correlationKey));
					long downloadedDurationMillis = variables
							.getLong(ConstantsPing.getBpmnExecutionVariableDownloadedDurationMillis(correlationKey));

					BigDecimal downloadSpeed = NetworkSpeedCalculator.calculate(downloadedBytes,
							downloadedDurationMillis, networkSpeedUnit);

					int uploadedBytes = variables
							.getInteger(ConstantsPing.getBpmnExecutionVariableUploadedBytes(correlationKey));
					long uploadedDurationMillis = variables
							.getLong(ConstantsPing.getBpmnExecutionVariableUploadedDurationMillis(correlationKey));

					BigDecimal uploadSpeed = NetworkSpeedCalculator.calculate(uploadedBytes, uploadedDurationMillis,
							networkSpeedUnit);

					task.addOutput(PingStatusGenerator.createPingStatusOutput(target, statusCode, errorMessageList,
							downloadSpeed, uploadSpeed, networkSpeedUnit));
				}

				task.addOutput(PingStatusGenerator.createPingStatusOutput(target, statusCode));

				if (ConstantsPing.CODESYSTEM_DSF_PING_STATUS_VALUE_PONG_MISSING.equals(statusCode))
					errorMailService.pongMessageNotReceived(task.getIdElement(), target);
			}
		});

		// TODO only send one combined status mail

		variables.updateTask(task);
	}
}
