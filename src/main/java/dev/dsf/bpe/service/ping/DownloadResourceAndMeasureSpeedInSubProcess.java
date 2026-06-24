package dev.dsf.bpe.service.ping;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.BinaryResourceDownloader;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.error.ServiceTaskErrorHandler;
import dev.dsf.bpe.v2.error.impl.DefaultServiceTaskErrorHandler;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class DownloadResourceAndMeasureSpeedInSubProcess implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResourceAndMeasureSpeedInSubProcess.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
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
			variables.setJsonVariable(ExecutionVariables.downloadedDuration.correlatedValue(correlationKey),
					downloadResult.getDownloadedDuration());
		}
		else
		{
			variables.setJsonVariableLocal(ExecutionVariables.resourceDownloadError.name(),
					downloadResult.getErrorTuple().errorLocal());
		}

		logger.debug("Completed resource download and measured speed.");
	}

	@Override
	public ServiceTaskErrorHandler getErrorHandler()
	{
		return new DefaultServiceTaskErrorHandler() {
			@Override
			public Exception handleException(ProcessPluginApi api, Variables variables, Exception exception)
			{
				logger.error("Unexpected error while downloading resource and measuring speed.", exception);
				String correlationKey = variables.getTarget().getCorrelationKey();
				ErrorListUtils.add(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null),
						variables, correlationKey);
				throw new ErrorBoundaryEvent(ConstantsPing.BPMN_ERROR_CODE_UNEXPECTED_ERROR, ConstantsPing.BPMN_ERROR_MESSAGE_UNEXPECTED_ERROR);
			}
		};
	}
}
