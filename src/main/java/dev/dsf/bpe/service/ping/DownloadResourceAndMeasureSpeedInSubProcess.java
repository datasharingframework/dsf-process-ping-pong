package dev.dsf.bpe.service.ping;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.service.AbstractService;
import dev.dsf.bpe.util.BinaryResourceDownloader;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Target;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.duration.DurationValueImpl;

public class DownloadResourceAndMeasureSpeedInSubProcess extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResourceAndMeasureSpeedInSubProcess.class);

	public DownloadResourceAndMeasureSpeedInSubProcess(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecuteWithErrorHandling(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		logger.debug("Starting resource download to measure speed...");

		Task task = variables.getLatestTask();
		Target target = variables.getTarget();
		String correlationKey = target.getCorrelationKey();


		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader(
				ConstantsPing.PROCESS_NAME_PING).download(variables, api, task);

		if (downloadResult.getErrorTuple() == null)
		{
			variables.setLong(ExecutionVariables.downloadedBytes.correlatedValue(correlationKey),
					downloadResult.getDownloadedBytes());
			variables.setVariable(ExecutionVariables.downloadedDuration.correlatedValue(correlationKey),
					new DurationValueImpl(downloadResult.getDownloadedDuration()));
		}
		else
		{
			delegateExecution.setVariableLocal(ExecutionVariables.resourceDownloadError.name(),
					downloadResult.getErrorTuple().errorLocal());
		}

		logger.debug("Completed resource download and measured speed.");
	}

	@Override
	protected void handleException(DelegateExecution execution, Variables variables, Exception exception)
			throws Exception
	{
		logger.error("Unexpected error while downloading resource and measuring speed.", exception);
		String correlationKey = variables.getTarget().getCorrelationKey();
		ErrorListUtils.add(
				new ProcessError(ConstantsPing.PROCESS_NAME_PING, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null),
				execution, correlationKey);
		throw new BpmnError(ConstantsPing.BPMN_ERROR_CODE_UNKNOWN_ERROR);
	}
}
