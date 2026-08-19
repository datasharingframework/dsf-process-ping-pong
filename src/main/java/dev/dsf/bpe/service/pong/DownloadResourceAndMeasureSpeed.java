package dev.dsf.bpe.service.pong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.BinaryResourceDownloader;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class DownloadResourceAndMeasureSpeed implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResourceAndMeasureSpeed.class);

	@Override
	public void execute(ProcessPluginApi api, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		logger.debug("Starting resource download to measure speed...");

		String downloadResourceReference = variables.getString(ExecutionVariables.downloadResourceReference.name());

		BinaryResourceDownloader.DownloadResult downloadResult = new BinaryResourceDownloader(
				ConstantsPing.PROCESS_NAME_PONG).download(variables, api, downloadResourceReference);

		if (downloadResult.getErrorTuple() == null)
		{
			variables.setLong(ExecutionVariables.downloadedBytes.name(), downloadResult.getDownloadedBytes());
			variables.setJsonVariable(ExecutionVariables.downloadedDuration.name(),
					downloadResult.getDownloadedDuration());
		}
		else
		{
			variables.setJsonVariable(ExecutionVariables.resourceDownloadError.name(),
					downloadResult.getErrorTuple().errorLocal());
			variables.setJsonVariable(ExecutionVariables.resourceDownloadErrorRemote.name(),
					downloadResult.getErrorTuple().errorRemote());
		}
		logger.debug("Completed resource download and measured speed.");
	}
}
