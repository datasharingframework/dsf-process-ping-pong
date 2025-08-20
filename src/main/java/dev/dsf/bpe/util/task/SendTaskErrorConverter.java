package dev.dsf.bpe.util.task;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public final class SendTaskErrorConverter
{
	private SendTaskErrorConverter()
	{
	}

	public static ProcessError convert(Exception exception, String action)
	{
		if (exception instanceof WebApplicationException e)
		{
			return convertWebApplicationException().apply(e, action);
		}
		else if (exception instanceof SSLHandshakeException e)
		{
			return convertSSLHandshakeException().apply(e, action);
		}
		else if (exception instanceof ConnectTimeoutException e)
		{
			return convertConnectTimeoutException().apply(e, action);
		}
		else if (exception instanceof HttpHostConnectException e)
		{
			return convertHttpHostConnectException().apply(e, action);
		}
		else if (exception instanceof ProcessingException e)
		{
			SSLHandshakeException sslHandshakeException = getExpectedCauseInstanceFromStack(SSLHandshakeException.class, e);
			if(sslHandshakeException != null)
			{
				return convertSSLHandshakeException().apply(sslHandshakeException, action);
			}

			ConnectTimeoutException connectTimeoutException = getExpectedCauseInstanceFromStack(ConnectTimeoutException.class, e);
			if(connectTimeoutException != null)
			{
				return convertConnectTimeoutException().apply(connectTimeoutException, action);
			}

			UnknownHostException unknownHostException = getExpectedCauseInstanceFromStack(UnknownHostException.class, e);
			if (unknownHostException != null)
			{
				return convertUnknownHostException().apply(unknownHostException, action);
			}

			HttpHostConnectException httpHostConnectException = getExpectedCauseInstanceFromStack(HttpHostConnectException.class, e);
			if (httpHostConnectException != null)
			{
				return convertHttpHostConnectException().apply(httpHostConnectException, action);
			}

			return convertExceptionFallback().apply(e, action);
		}
		else
		{
			return convertExceptionFallback().apply(exception, action);
		}
	}

	private static BiFunction<SSLHandshakeException, String, ProcessError> convertSSLHandshakeException()
	{
		return (e, action) -> new ProcessError(CodeSystem.DsfPingProcesses.Code.PING,
				CodeSystem.DsfPingProcessSteps.Code.PING, action, ConstantsPing.POTENTIAL_FIX_URL_ERROR_SSL,
				e.getMessage());
	}

	private static BiFunction<Exception, String, ProcessError> convertExceptionFallback()
	{
		return (e, action) -> new ProcessError(CodeSystem.DsfPingProcesses.Code.PING,
				CodeSystem.DsfPingProcessSteps.Code.PING, action, null, e.getMessage());
	}

	private static BiFunction<ConnectTimeoutException, String, ProcessError> convertConnectTimeoutException()
	{
		return (e, action) -> new ProcessError(CodeSystem.DsfPingProcesses.Code.PING,
				CodeSystem.DsfPingProcessSteps.Code.PING, action, ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT,
				e.getMessage());
	}

	private static BiFunction<WebApplicationException, String, ProcessError> convertWebApplicationException()
	{
		return (e, action) -> new ProcessError(CodeSystem.DsfPingProcesses.Code.PING,
				CodeSystem.DsfPingProcessSteps.Code.PING, action, ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP,
				"Response from remote DSF FHIR server: " + e.getResponse().getStatus() + " "
						+ Response.Status.fromStatusCode(e.getResponse().getStatus()).toString());
	}

	private static BiFunction<HttpHostConnectException, String, ProcessError> convertHttpHostConnectException()
	{
		return (e, action) -> new ProcessError(CodeSystem.DsfPingProcesses.Code.PING,
				CodeSystem.DsfPingProcessSteps.Code.PING, action, ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED,
				e.getMessage());
	}

	private static BiFunction<UnknownHostException, String, ProcessError> convertUnknownHostException()
	{
		return (e, action) -> new ProcessError(CodeSystem.DsfPingProcesses.Code.PING,
				CodeSystem.DsfPingProcessSteps.Code.PING, action, ConstantsPing.POTENTIAL_FIX_URL_UNKNOWN_HOST,
				e.getMessage());
	}

	private static <T extends Throwable> T getExpectedCauseInstanceFromStack(Class<T> expectedCause, Throwable e)
	{
		if (Objects.isNull(e))
			return null;
		if (expectedCause.isInstance(e))
			return expectedCause.cast(e);
		return getExpectedCauseInstanceFromStack(expectedCause, e.getCause());
	}
}
