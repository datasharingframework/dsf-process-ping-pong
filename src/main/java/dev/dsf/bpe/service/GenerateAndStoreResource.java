package dev.dsf.bpe.service;

import java.net.SocketTimeoutException;
import java.util.Locale;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import dev.dsf.bpe.variables.process_error.ProcessErrorValueImpl;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

public class GenerateAndStoreResource extends AbstractService implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(GenerateAndStoreResource.class);
	private final long maxUploadSizeBytes;
	private Expression process;

	public GenerateAndStoreResource(ProcessPluginApi api, long maxUploadSizeBytes)
	{
		super(api);
		this.maxUploadSizeBytes = maxUploadSizeBytes;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
	}

	public void doExecuteWithErrorHandling(DelegateExecution delegateExecution, Variables variables) throws BpmnError
	{
		logger.debug("Generating resource...");
		long downloadResourceSizeBytes = getDownloadResourceSize(variables);
		String process = (String) this.process.getValue(delegateExecution);
		RandomByteInputStream resourceContent;
		if (downloadResourceSizeBytes > maxUploadSizeBytes)
		{
			logger.info(
					"Requested resource size of {} bytes exceeds configured maximum upload size of {} bytes. Trimmed to maximum upload size.",
					downloadResourceSizeBytes, maxUploadSizeBytes);
			setDownloadResourceSizeBytes(variables, maxUploadSizeBytes);
			resourceContent = new RandomByteInputStream(maxUploadSizeBytes);
		}
		else
		{
			setDownloadResourceSizeBytes(variables, downloadResourceSizeBytes);
			resourceContent = new RandomByteInputStream(downloadResourceSizeBytes);
		}
		logger.debug("Generated resource.");
		logger.debug("Storing binary resource for download...");

		try
		{
			IdType downloadResource = storeBinary(resourceContent);

			String reference = downloadResource.toVersionless().getValueAsString();

			variables.setString(ExecutionVariables.downloadResourceReference.name(), reference);

			logger.debug("Stored binary resource for download");
		}
		catch (WebApplicationException e)
		{
			int status = e.getResponse().getStatus();
			ProcessError error;
			ProcessError errorRemote;

			switch (status)
			{
				case 401:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_401,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_HTTP_401,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				case 403:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_403,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_HTTP_403,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				case 407:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_407,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_HTTP_407,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				case 413:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_413,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_HTTP_413,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				case 500:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_500,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_HTTP_500,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				case 502:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_502,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_HTTP_502,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				case 503:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_503,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_HTTP_503,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				case 504:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_504,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_HTTP_504,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
				default:
					error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_UNEXPECTED,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					errorRemote = new ProcessError(process,
							CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_HTTP_UNEXPECTED,
							ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP);
					break;
			}

			variables.setVariable(ExecutionVariables.resourceUploadError.name(), new ProcessErrorValueImpl(error));
			if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
			{
				variables.setVariable(ExecutionVariables.resourceUploadErrorRemote.name(),
						new ProcessErrorValueImpl(errorRemote));
			}
		}
		catch (ProcessingException e)
		{
			if (e.getCause() instanceof SocketTimeoutException socketTimeoutException)
			{
				ProcessError error = toProcessErrorLocal(socketTimeoutException, process);
				ProcessError errorRemote = toProcessErrorRemote(socketTimeoutException, process);

				variables.setVariable(ExecutionVariables.resourceUploadError.name(), new ProcessErrorValueImpl(error));
				if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
				{
					variables.setVariable(ExecutionVariables.resourceUploadErrorRemote.name(),
							new ProcessErrorValueImpl(errorRemote));
				}
			}
			else if (e.getCause() instanceof ConnectTimeoutException)
			{
				ProcessError error = toProcessErrorLocalConnectTimeout(process);
				ProcessError errorRemote = toProcessErrorRemoteConnectTimeout(process);

				variables.setVariable(ExecutionVariables.resourceUploadError.name(), new ProcessErrorValueImpl(error));
				if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
				{
					variables.setVariable(ExecutionVariables.resourceUploadErrorRemote.name(),
							new ProcessErrorValueImpl(errorRemote));
				}
			}
			else if (e.getCause() instanceof HttpHostConnectException)
			{
				ProcessError error = toProcessErrorLocalHttpHostConnect(process);
				ProcessError errorRemote = toProcessErrorRemoteHttpHostConnect(process);

				variables.setVariable(ExecutionVariables.resourceUploadError.name(), new ProcessErrorValueImpl(error));
				if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
				{
					variables.setVariable(ExecutionVariables.resourceUploadErrorRemote.name(),
							new ProcessErrorValueImpl(errorRemote));
				}
			}
			else
			{
				ProcessError error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null);
				ProcessError errorRemote = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_UNKNOWN,
						null);
				variables.setVariable(ExecutionVariables.resourceUploadError.name(), new ProcessErrorValueImpl(error));
				if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
				{
					variables.setVariable(ExecutionVariables.resourceUploadErrorRemote.name(),
							new ProcessErrorValueImpl(errorRemote));
				}
				logger.error("Unexpected error: {}", e.getMessage());
			}
		}
	}

	private ProcessError toProcessErrorLocal(SocketTimeoutException timeoutException, String process)
	{
		ProcessError error;
		String message = timeoutException.getMessage().toLowerCase(Locale.ROOT);
		if (message.contains("connect"))
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_TIMEOUT_CONNECT,
					ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
		}
		else if (message.contains("read"))
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_TIMEOUT_READ,
					ConstantsPing.POTENTIAL_FIX_URL_READ_TIMEOUT);
		}
		else
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null);
			logger.error("Unexpected error: {}", message);
		}
		return error;
	}

	private ProcessError toProcessErrorRemote(SocketTimeoutException timeoutException, String process)
	{
		ProcessError error;
		String message = timeoutException.getMessage().toLowerCase(Locale.ROOT);
		if (message.contains("connect"))
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_TIMEOUT_CONNECT,
					ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
		}
		else if (message.contains("read"))
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_TIMEOUT_READ,
					ConstantsPing.POTENTIAL_FIX_URL_READ_TIMEOUT);
		}
		else
		{
			error = new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_UNKNOWN, null);
			logger.error("Unexpected error: {}", message);
		}
		return error;
	}

	private ProcessError toProcessErrorLocalHttpHostConnect(String process)
	{
		return new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_HTTP_HOST_CONNECT,
				ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED);
	}

	private ProcessError toProcessErrorRemoteHttpHostConnect(String process)
	{
		return new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_HTTP_HOST_CONNECT,
				ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED);
	}

	private ProcessError toProcessErrorLocalConnectTimeout(String process)
	{
		return new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_BINARY_POST_TIMEOUT_CONNECT,
				ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
	}

	private ProcessError toProcessErrorRemoteConnectTimeout(String process)
	{
		return new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_BINARY_POST_TIMEOUT_CONNECT,
				ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
	}

	private long getDownloadResourceSize(Variables variables)
	{
		return variables.getLong(ExecutionVariables.downloadResourceSizeBytes.name());
	}

	private void setDownloadResourceSizeBytes(Variables variables, long resourceSizeBytes)
	{
		variables.setLong(ExecutionVariables.downloadResourceSizeBytes.name(), resourceSizeBytes);
	}

	private IdType storeBinary(RandomByteInputStream downloadResourceContent)
	{
		return api.getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().createBinary(
				downloadResourceContent, ConstantsPing.DOWNLOAD_RESOURCE_MIME_TYPE,
				api.getOrganizationProvider().getLocalOrganization().get().getIdElement().getValue());
	}

	public void setProcess(Expression process)
	{
		this.process = process;
	}
}
