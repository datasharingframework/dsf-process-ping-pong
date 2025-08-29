package dev.dsf.bpe.util.task;

import java.net.UnknownHostException;
import java.util.Objects;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ProcessError;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

public final class SendTaskErrorConverter
{
	private static final Logger logger = LoggerFactory.getLogger(SendTaskErrorConverter.class);

	private SendTaskErrorConverter()
	{
	}

	private enum ErrorType
	{
		LOCAL,
		REMOTE
	}

	public static ProcessError convertLocal(Exception exception, boolean messageWithReference, String process)
	{
		return convert(exception, ErrorType.LOCAL, messageWithReference, process);
	}

	public static ProcessError convertRemote(Exception exception, boolean messageWithReference, String process)
	{
		return convert(exception, ErrorType.REMOTE, messageWithReference, process);
	}

	private static ProcessError convert(Exception exception, ErrorType errorType, boolean messageWithReference,
			String process)
	{
		if (exception instanceof WebApplicationException e)
		{
			return convertWebApplicationException(e, errorType, messageWithReference, process);
		}
		else if (exception instanceof SSLHandshakeException)
		{
			return convertSSLHandshakeException(errorType, process);
		}
		else if (exception instanceof ConnectTimeoutException)
		{
			return convertConnectTimeoutException(errorType, process);
		}
		else if (exception instanceof HttpHostConnectException)
		{
			return convertHttpHostConnectException(errorType, process);
		}
		else if (exception instanceof ProcessingException e)
		{
			SSLHandshakeException sslHandshakeException = getExpectedCauseInstanceFromStack(SSLHandshakeException.class,
					e);
			if (sslHandshakeException != null)
			{
				return convertSSLHandshakeException(errorType, process);
			}

			ConnectTimeoutException connectTimeoutException = getExpectedCauseInstanceFromStack(
					ConnectTimeoutException.class, e);
			if (connectTimeoutException != null)
			{
				return convertConnectTimeoutException(errorType, process);
			}

			UnknownHostException unknownHostException = getExpectedCauseInstanceFromStack(UnknownHostException.class,
					e);
			if (unknownHostException != null)
			{
				return convertUnknownHostException(errorType, process);
			}

			HttpHostConnectException httpHostConnectException = getExpectedCauseInstanceFromStack(
					HttpHostConnectException.class, e);
			if (httpHostConnectException != null)
			{
				return convertHttpHostConnectException(errorType, process);
			}

			return convertExceptionFallback(exception, errorType, process);
		}
		else
		{
			return convertExceptionFallback(exception, errorType, process);
		}
	}

	private static ProcessError convertSSLHandshakeException(ErrorType errorType, String process)
	{
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.SEND_MESSAGE_SSL_HANDSHAKE,
						ConstantsPing.POTENTIAL_FIX_URL_ERROR_SSL);
				case REMOTE -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_SSL_HANDSHAKE,
						ConstantsPing.POTENTIAL_FIX_URL_ERROR_SSL);
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
						CodeSystem.DsfPingError.Concept.SEND_MESSAGE_SSL_HANDSHAKE,
						ConstantsPing.POTENTIAL_FIX_URL_ERROR_SSL);
				case REMOTE -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
						CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_SSL_HANDSHAKE,
						ConstantsPing.POTENTIAL_FIX_URL_ERROR_SSL);
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
	}

	private static ProcessError convertExceptionFallback(Exception e, ErrorType errorType, String process)
	{
		logger.warn("No fitting converter found for exception {}: {}", e.getClass().getName(), e.getMessage());
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL, REMOTE -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.UNKNOWN, null);
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL, REMOTE -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
						CodeSystem.DsfPingError.Concept.UNKNOWN, null);
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
	}

	private static ProcessError convertConnectTimeoutException(ErrorType errorType, String process)
	{
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.SEND_MESSAGE_CONNECT_TIMEOUT,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
				case REMOTE -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_CONNECT_TIMEOUT,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
						CodeSystem.DsfPingError.Concept.SEND_MESSAGE_CONNECT_TIMEOUT,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
				case REMOTE -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
						CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_CONNECT_TIMEOUT,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
	}

	private static ProcessError convertWebApplicationException(WebApplicationException e, ErrorType errorType,
			boolean messageWithReference, String process)
	{
		int statusCode = e.getResponse().getStatus();
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> switch (statusCode)
				{
					case 401 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
							CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_401,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					case 403 ->
					{
						if (messageWithReference)
						{
							yield new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.SEND_REFERENCE_MESSAGE_HTTP_403,
									ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
						}
						else
						{
							yield new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_403,
									ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
						}
					}
					case 500 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
							CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_500,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					case 502 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
							CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_502,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					default -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
							CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_UNEXPECTED,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
				};
				case REMOTE -> switch (statusCode)
				{
					case 401 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
							CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_401,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					case 403 ->
					{
						if (messageWithReference)
						{
							yield new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.RECEIVE_REFERENCE_MESSAGE_HTTP_403,
									ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
						}
						else
						{
							yield new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_403,
									ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
						}
					}
					case 500 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
							CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_500,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					case 502 -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
							CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_502,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					default -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
							CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_UNEXPECTED,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
				};
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> switch (statusCode)
				{
					case 401 -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
							CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_401,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					case 403 -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
							CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_403,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					case 500 -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
							CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_500,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					case 502 -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
							CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_502,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					default -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
							CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_UNEXPECTED,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
				};
				case REMOTE -> switch (statusCode)
				{
					case 401 -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
							CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_401,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					case 403 -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
							CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_403,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					case 500 -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
							CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_500,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					case 502 -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
							CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_502,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
					default -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
							CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_UNEXPECTED,
							ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
				};
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
	}

	private static ProcessError convertHttpHostConnectException(ErrorType errorType, String process)
	{
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_HOST_CONNECT,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED);
				case REMOTE -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_HOST_CONNECT,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED);
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
						CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_HOST_CONNECT,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED);
				case REMOTE -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
						CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_HOST_CONNECT,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED);
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
	}

	private static ProcessError convertUnknownHostException(ErrorType errorType, String process)
	{
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.SEND_MESSAGE_UNKNOWN_HOST,
						ConstantsPing.POTENTIAL_FIX_URL_UNKNOWN_HOST);
				case REMOTE -> new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_UNKNOWN_HOST,
						ConstantsPing.POTENTIAL_FIX_URL_UNKNOWN_HOST);
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
						CodeSystem.DsfPingError.Concept.SEND_MESSAGE_UNKNOWN_HOST,
						ConstantsPing.POTENTIAL_FIX_URL_UNKNOWN_HOST);
				case REMOTE -> new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
						CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_UNKNOWN_HOST,
						ConstantsPing.POTENTIAL_FIX_URL_UNKNOWN_HOST);
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
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
