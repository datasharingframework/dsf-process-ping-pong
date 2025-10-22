package dev.dsf.bpe.util.task;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;
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

	public record ProcessErrorWithStatusCode(ProcessError error, CodeSystem.DsfPingStatus.Code statusCode)
	{
	}

	public static ProcessErrorWithStatusCode convertLocal(Exception exception, boolean messageWithReference,
			String process)
	{
		return convert(exception, ErrorType.LOCAL, messageWithReference, process);
	}

	private static ProcessErrorWithStatusCode convert(Exception exception, ErrorType errorType,
			boolean messageWithReference, String process)
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
		else if (exception instanceof SocketTimeoutException socketTimeoutException)
		{
			return convertSocketTimeoutException(errorType, process, socketTimeoutException);
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

			SocketTimeoutException socketTimeoutException = getExpectedCauseInstanceFromStack(
					SocketTimeoutException.class, e);
			if (socketTimeoutException != null)
			{
				return convertSocketTimeoutException(errorType, process, socketTimeoutException);
			}

			return convertExceptionFallback(exception, errorType, process);
		}
		else
		{
			return convertExceptionFallback(exception, errorType, process);
		}
	}

	private static ProcessErrorWithStatusCode convertSocketTimeoutException(ErrorType errorType, String process,
			SocketTimeoutException socketTimeoutException)
	{
		ProcessError error;
		String message = socketTimeoutException.getMessage().toLowerCase(Locale.ROOT);
		if (message.contains("connect"))
		{
			error = switch (errorType)
			{
				case LOCAL -> new ProcessError(process,
						CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_TIMEOUT_CONNECT,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
				case REMOTE -> new ProcessError(process,
						CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_TIMEOUT_CONNECT,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
			};
			return new ProcessErrorWithStatusCode(error, CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
		}
		else if (message.contains("read"))
		{
			error = switch (errorType)
			{
				case LOCAL -> new ProcessError(process,
						CodeSystem.DsfPingError.Concept.LOCAL_BINARY_DOWNLOAD_TIMEOUT_READ,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
				case REMOTE -> new ProcessError(process,
						CodeSystem.DsfPingError.Concept.REMOTE_BINARY_DOWNLOAD_TIMEOUT_READ,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
			};
			return new ProcessErrorWithStatusCode(error, CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
		}
		else
		{
			error = switch (errorType)
			{
				case LOCAL -> new ProcessError(process, CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
				case REMOTE -> new ProcessError(process, CodeSystem.DsfPingError.Concept.REMOTE_UNKNOWN,
						ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT);
			};
			logger.error("Unexpected error: {}", socketTimeoutException.getMessage());
			return new ProcessErrorWithStatusCode(error, CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
		}

	}

	private static ProcessErrorWithStatusCode convertSSLHandshakeException(ErrorType errorType, String process)
	{
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING,
								CodeSystem.DsfPingError.Concept.SEND_MESSAGE_SSL_HANDSHAKE,
								ConstantsPing.POTENTIAL_FIX_URL_ERROR_SSL),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				case REMOTE -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING,
								CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_SSL_HANDSHAKE,
								ConstantsPing.POTENTIAL_FIX_URL_ERROR_SSL),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
								CodeSystem.DsfPingError.Concept.SEND_MESSAGE_SSL_HANDSHAKE,
								ConstantsPing.POTENTIAL_FIX_URL_ERROR_SSL),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				case REMOTE -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
								CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_SSL_HANDSHAKE,
								ConstantsPing.POTENTIAL_FIX_URL_ERROR_SSL),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
	}

	private static ProcessErrorWithStatusCode convertExceptionFallback(Exception e, ErrorType errorType, String process)
	{
		logger.warn("No fitting converter found for exception {}: {}", e.getClass().getName(), e.getMessage());
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessErrorWithStatusCode(new ProcessError(ConstantsPing.PROCESS_NAME_PING,
						CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				case REMOTE -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING,
								CodeSystem.DsfPingError.Concept.REMOTE_UNKNOWN, null),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessErrorWithStatusCode(new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
						CodeSystem.DsfPingError.Concept.LOCAL_UNKNOWN, null),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				case REMOTE -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
								CodeSystem.DsfPingError.Concept.REMOTE_UNKNOWN, null),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
	}

	private static ProcessErrorWithStatusCode convertConnectTimeoutException(ErrorType errorType, String process)
	{
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING,
								CodeSystem.DsfPingError.Concept.SEND_MESSAGE_CONNECT_TIMEOUT,
								ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				case REMOTE -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING,
								CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_CONNECT_TIMEOUT,
								ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
								CodeSystem.DsfPingError.Concept.SEND_MESSAGE_CONNECT_TIMEOUT,
								ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				case REMOTE -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
								CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_CONNECT_TIMEOUT,
								ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_TIMEOUT),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
	}

	private static ProcessErrorWithStatusCode convertWebApplicationException(WebApplicationException e,
			ErrorType errorType, boolean messageWithReference, String process)
	{
		int statusCode = e.getResponse().getStatus();
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> switch (statusCode)
				{
					case 401 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_401,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_ALLOWED);
					case 403 -> {
						if (messageWithReference)
						{
							yield new ProcessErrorWithStatusCode(
									new ProcessError(ConstantsPing.PROCESS_NAME_PING,
											CodeSystem.DsfPingError.Concept.SEND_REFERENCE_MESSAGE_HTTP_403,
											ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
									CodeSystem.DsfPingStatus.Code.NOT_ALLOWED);
						}
						else
						{
							yield new ProcessErrorWithStatusCode(
									new ProcessError(ConstantsPing.PROCESS_NAME_PING,
											CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_403,
											ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
									CodeSystem.DsfPingStatus.Code.NOT_ALLOWED);
						}
					}
					case 500 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_500,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					case 502 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_502,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					case 504 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_504,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					default -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_UNEXPECTED, null),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				};
				case REMOTE -> switch (statusCode)
				{
					case 401 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_401,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_ALLOWED);
					case 403 -> {
						if (messageWithReference)
						{
							yield new ProcessErrorWithStatusCode(
									new ProcessError(ConstantsPing.PROCESS_NAME_PING,
											CodeSystem.DsfPingError.Concept.RECEIVE_REFERENCE_MESSAGE_HTTP_403,
											ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
									CodeSystem.DsfPingStatus.Code.NOT_ALLOWED);
						}
						else
						{
							yield new ProcessErrorWithStatusCode(
									new ProcessError(ConstantsPing.PROCESS_NAME_PING,
											CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_403,
											ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
									CodeSystem.DsfPingStatus.Code.NOT_ALLOWED);
						}
					}
					case 500 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_500,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					case 502 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_502,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					case 504 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_504,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					default -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PING,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_UNEXPECTED, null),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				};
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> switch (statusCode)
				{
					case 401 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_401,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_ALLOWED);
					case 403 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_403,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_ALLOWED);
					case 500 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_500,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					case 502 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_502,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					case 504 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_504,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					default -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_UNEXPECTED, null),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				};
				case REMOTE -> switch (statusCode)
				{
					case 401 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_401,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_ALLOWED);
					case 403 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_403,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_ALLOWED);
					case 500 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_500,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					case 502 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_502,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					case 504 -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_504,
									ConstantsPing.POTENTIAL_FIX_URL_ERROR_HTTP),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
					default -> new ProcessErrorWithStatusCode(
							new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
									CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_UNEXPECTED, null),
							CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				};
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
	}

	private static ProcessErrorWithStatusCode convertHttpHostConnectException(ErrorType errorType, String process)
	{
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING,
								CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_HOST_CONNECT,
								ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				case REMOTE -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING,
								CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_HOST_CONNECT,
								ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
								CodeSystem.DsfPingError.Concept.SEND_MESSAGE_HTTP_HOST_CONNECT,
								ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				case REMOTE -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
								CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_HTTP_HOST_CONNECT,
								ConstantsPing.POTENTIAL_FIX_URL_CONNECTION_REFUSED),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
			};
		}
		else
		{
			throw new IllegalArgumentException("Unknown process: " + process);
		}
	}

	private static ProcessErrorWithStatusCode convertUnknownHostException(ErrorType errorType, String process)
	{
		if (ConstantsPing.PROCESS_NAME_PING.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING,
								CodeSystem.DsfPingError.Concept.SEND_MESSAGE_UNKNOWN_HOST,
								ConstantsPing.POTENTIAL_FIX_URL_UNKNOWN_HOST),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				case REMOTE -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PING,
								CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_UNKNOWN_HOST,
								ConstantsPing.POTENTIAL_FIX_URL_UNKNOWN_HOST),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
			};
		}
		else if (ConstantsPing.PROCESS_NAME_PONG.equals(process))
		{
			return switch (errorType)
			{
				case LOCAL -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
								CodeSystem.DsfPingError.Concept.SEND_MESSAGE_UNKNOWN_HOST,
								ConstantsPing.POTENTIAL_FIX_URL_UNKNOWN_HOST),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
				case REMOTE -> new ProcessErrorWithStatusCode(
						new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
								CodeSystem.DsfPingError.Concept.RECEIVE_MESSAGE_UNKNOWN_HOST,
								ConstantsPing.POTENTIAL_FIX_URL_UNKNOWN_HOST),
						CodeSystem.DsfPingStatus.Code.NOT_REACHABLE);
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
