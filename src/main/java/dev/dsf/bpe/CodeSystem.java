package dev.dsf.bpe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r4.model.Coding;

public final class CodeSystem
{
	private CodeSystem()
	{
	}

	interface SingleStringValueEnum
	{
		String getValue();
	}

	protected static class SingleStringValueEnumParser<T extends Enum<T> & SingleStringValueEnum>
	{
		private final Class<T> enumClass;

		public SingleStringValueEnumParser(Class<T> enumClass)
		{
			this.enumClass = enumClass;
		}

		public T ofValue(String value)
		{
			try
			{
				return T.valueOf(enumClass, value);
			}
			catch (IllegalArgumentException e)
			{
				for (T t : enumClass.getEnumConstants())
				{
					if (t.getValue().equals(value))
					{
						return t;
					}
				}
				throw new IllegalArgumentException("Unable to convert " + value + " to " + enumClass.getName());
			}
		}
	}

	public static final class DsfPing
	{
		public static final String URL = "http://dsf.dev/fhir/CodeSystem/ping";

		private DsfPing()
		{
		}

		public static Coding fromCode(Code code)
		{
			return new Coding().setSystem(URL).setCode(code.getValue())
					.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);
		}

		public enum Code implements SingleStringValueEnum
		{
			PING_STATUS("ping-status"),
			PONG_STATUS("pong-status"),
			ENDPOINT_IDENTIFIER("endpoint-identifier"),
			TARGET_ENDPOINTS("target-endpoints"),
			TIMER_INTERVAL("timer-interval"),
			DOWNLOAD_RESOURCE_SIZE_BYTES("download-resource-size-bytes"),
			DOWNLOADED_DURATION_MILLIS("downloaded-duration"),
			DOWNLOADED_BYTES("downloaded-bytes"),
			DOWNLOAD_RESOURCE_REFERENCE("download-resource-reference"),
			ERROR("error");

			private final String value;

			Code(String value)
			{
				this.value = value;
			}

			public String getValue()
			{
				return value;
			}

			public static Code ofValue(String value)
			{
				return new SingleStringValueEnumParser<>(Code.class).ofValue(value);
			}
		}
	}

	public static final class DsfPingStatus
	{
		public static final String URL = "http://dsf.dev/fhir/CodeSystem/ping-status";

		private DsfPingStatus()
		{
		}

		public static Coding fromCode(Code code)
		{
			return new Coding().setSystem(URL).setCode(code.getValue())
					.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);
		}

		public enum Code implements SingleStringValueEnum
		{
			NOT_ALLOWED("not-allowed"),
			NOT_REACHABLE("not-reachable"),
			PONG_MISSING("pong-missing"),
			PONG_RECEIVED("pong-received"),
			PONG_SENT("pong-send");

			private final String value;

			Code(String value)
			{
				this.value = value;
			}

			public String getValue()
			{
				return value;
			}

			public static Code ofValue(String value)
			{
				return new SingleStringValueEnumParser<>(Code.class).ofValue(value);
			}
		}
	}

	// TODO: rename to DsfNetworkSpeedUnits
	public static final class DsfPingUnits
	{
		public static final String URL = "http://dsf.dev/fhir/CodeSystem/ping-units";

		private DsfPingUnits()
		{
		}

		public static Coding fromCode(Code code)
		{
			return new Coding().setSystem(URL).setCode(code.toUcum())
					.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);
		}

		public enum Code
		{
			bps
			{
				@Override
				public BigDecimal calculateSpeed(long bytes, Duration duration)
				{
					if (bytes == 0)
						return BigDecimal.ZERO;
					if (duration.isZero())
						return BigDecimal.valueOf(Long.MAX_VALUE);
					BigDecimal bits = BigDecimal.valueOf(bytes * 8L).setScale(2, RoundingMode.HALF_UP);
					BigDecimal seconds = BigDecimal.valueOf(duration.toMillis()).setScale(3, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000).setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
					return bits.divide(seconds, 2, RoundingMode.HALF_UP);
				}

				@Override
				public String toUcum()
				{
					return "bit/s";
				}
			},
			kbps
			{
				@Override
				public BigDecimal calculateSpeed(long bytes, Duration duration)
				{
					if (bytes == 0)
						return BigDecimal.ZERO;
					if (duration.isZero())
						return BigDecimal.valueOf(Long.MAX_VALUE);
					BigDecimal kiloBits = BigDecimal.valueOf(bytes * 8L).setScale(2, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP);
					BigDecimal seconds = BigDecimal.valueOf(duration.toMillis()).setScale(3, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000).setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
					return kiloBits.divide(seconds, 2, RoundingMode.HALF_UP);
				}

				@Override
				public String toUcum()
				{
					return "Kbit/s";
				}
			},
			Mbps
			{
				@Override
				public BigDecimal calculateSpeed(long bytes, Duration duration)
				{
					if (bytes == 0)
						return BigDecimal.ZERO;
					if (duration.isZero())
						return BigDecimal.valueOf(Long.MAX_VALUE);
					BigDecimal kiloBits = BigDecimal.valueOf(bytes * 8L).setScale(2, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000000), RoundingMode.HALF_UP);
					BigDecimal seconds = BigDecimal.valueOf(duration.toMillis()).setScale(3, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000).setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
					return kiloBits.divide(seconds, 2, RoundingMode.HALF_UP);
				}

				@Override
				public String toUcum()
				{
					return "Mbit/s";
				}
			},
			Gbps
			{
				@Override
				public BigDecimal calculateSpeed(long bytes, Duration duration)
				{
					if (bytes == 0)
						return BigDecimal.ZERO;
					if (duration.isZero())
						return BigDecimal.valueOf(Long.MAX_VALUE);
					BigDecimal kiloBits = BigDecimal.valueOf(bytes * 8L).setScale(2, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000000000), RoundingMode.HALF_UP);
					BigDecimal seconds = BigDecimal.valueOf(duration.toMillis()).setScale(3, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000).setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
					return kiloBits.divide(seconds, 2, RoundingMode.HALF_UP);
				}

				@Override
				public String toUcum()
				{
					return "Gbit/s";
				}
			},
			Bps
			{
				@Override
				public BigDecimal calculateSpeed(long bytes, Duration duration)
				{
					if (bytes == 0)
						return BigDecimal.ZERO;
					if (duration.isZero())
						return BigDecimal.valueOf(Long.MAX_VALUE);
					BigDecimal kiloBits = BigDecimal.valueOf(bytes).setScale(2, RoundingMode.HALF_UP);
					BigDecimal seconds = BigDecimal.valueOf(duration.toMillis()).setScale(3, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000).setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
					return kiloBits.divide(seconds, 2, RoundingMode.HALF_UP);
				}

				@Override
				public String toUcum()
				{
					return "By/s";
				}
			},
			kBps
			{
				@Override
				public BigDecimal calculateSpeed(long bytes, Duration duration)
				{
					if (bytes == 0)
						return BigDecimal.ZERO;
					if (duration.isZero())
						return BigDecimal.valueOf(Long.MAX_VALUE);
					BigDecimal kiloBits = BigDecimal.valueOf(bytes).setScale(2, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP);
					BigDecimal seconds = BigDecimal.valueOf(duration.toMillis()).setScale(3, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000).setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
					return kiloBits.divide(seconds, 2, RoundingMode.HALF_UP);
				}

				@Override
				public String toUcum()
				{
					return "KBy/s";
				}
			},
			MBps
			{
				@Override
				public BigDecimal calculateSpeed(long bytes, Duration duration)
				{
					if (bytes == 0)
						return BigDecimal.ZERO;
					if (duration.isZero())
						return BigDecimal.valueOf(Long.MAX_VALUE);
					BigDecimal kiloBits = BigDecimal.valueOf(bytes).setScale(2, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000000), RoundingMode.HALF_UP);
					BigDecimal seconds = BigDecimal.valueOf(duration.toMillis()).setScale(3, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000).setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
					return kiloBits.divide(seconds, 2, RoundingMode.HALF_UP);
				}

				@Override
				public String toUcum()
				{
					return "MBy/s";
				}
			},
			GBps
			{
				@Override
				public BigDecimal calculateSpeed(long bytes, Duration duration)
				{
					if (bytes == 0)
						return BigDecimal.ZERO;
					if (duration.isZero())
						return BigDecimal.valueOf(Long.MAX_VALUE);
					BigDecimal kiloBits = BigDecimal.valueOf(bytes).setScale(2, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000000000), RoundingMode.HALF_UP);
					BigDecimal seconds = BigDecimal.valueOf(duration.toMillis()).setScale(3, RoundingMode.HALF_UP)
							.divide(BigDecimal.valueOf(1000).setScale(3, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
					return kiloBits.divide(seconds, 2, RoundingMode.HALF_UP);
				}

				@Override
				public String toUcum()
				{
					return "GBy/s";
				}
			};

			public abstract BigDecimal calculateSpeed(long bytes, Duration duration);

			public abstract String toUcum();
		}
	}

	public final class DsfPingError
	{
		public static final String URL = "http://dsf.dev/fhir/CodeSystem/ping-error";

		private DsfPingError()
		{
		}

		public static Coding fromConcept(Concept concept)
		{
			return new Coding().setSystem(URL).setCode(concept.getCode()).setDisplay(concept.getDisplay())
					.setVersion(PingProcessPluginDefinition.RESOURCE_VERSION);
		}

		public enum Concept
		{
			SEND_MESSAGE_HTTP_401(
					"send-message-http-401",
					"Sending a message to the remote instance resulted in HTTP status 401"
			),
			SEND_MESSAGE_HTTP_403(
					"send-message-http-403",
					"Sending a message to the remote instance resulted in HTTP status 403"
			),
			SEND_REFERENCE_MESSAGE_HTTP_403(
					"send-reference-message-http-403",
					"Sending a message including a reference to the remote instance resulted in HTTP status 403"
			),
			SEND_MESSAGE_HTTP_500(
					"send-message-http-500",
					"Sending a message to the remote instance resulted in HTTP status 500"
			),
			SEND_MESSAGE_HTTP_502(
					"send-message-http-502",
					"Sending a message to the remote instance resulted in HTTP status 502"
			),
			SEND_MESSAGE_HTTP_UNEXPECTED(
					"send-message-http-unexpected",
					"Sending a message to the remote instance resulted in an unexpected HTTP status code"
			),
			SEND_MESSAGE_SSL_HANDSHAKE(
					"send-message-ssl-handshake",
					"Sending a message to the remote instance was unsuccessful because of a failed SSL handshake"
			),
			SEND_MESSAGE_CONNECT_TIMEOUT(
					"send-message-connect-timeout",
					"Sending a message to the remote instance was unsuccessful because of a connection timeout"
			),
			SEND_MESSAGE_HTTP_HOST_CONNECT(
					"send-message-http-host-connect",
					"Sending a message to the remote instance was unsuccessful because the connection was refused"
			),
			SEND_MESSAGE_UNKNOWN_HOST(
					"send-message-unknown-host",
					"Sending a message to the remote instance was unsuccessful because the target hostname could not be resolved"
			),

			RECEIVE_MESSAGE_HTTP_401(
					"receive-message-http-401",
					"Received a message and responded with HTTP status 401"
			),
			RECEIVE_MESSAGE_HTTP_403(
					"receive-message-http-403",
					"Received a message and responded with HTTP status 403"
			),
			RECEIVE_REFERENCE_MESSAGE_HTTP_403(
					"receive-reference-message-http-403",
					"Received a message including a reference and responded with HTTP status 403"
			),
			RECEIVE_MESSAGE_HTTP_500(
					"receive-message-http-500",
					"Received a message and responded with HTTP status 500"
			),
			RECEIVE_MESSAGE_HTTP_502(
					"receive-message-http-502",
					"Received a message and responded with HTTP status 502"
			),
			RECEIVE_MESSAGE_HTTP_UNEXPECTED(
					"receive-message-http-unexpected",
					"Received a message and responded with an unexpected HTTP status code"
			),
			RECEIVE_MESSAGE_SSL_HANDSHAKE(
					"receive-message-ssl-handshake",
					"Receiving a message was unsuccessful because of a failed SSL handshake"
			),
			RECEIVE_MESSAGE_CONNECT_TIMEOUT(
					"receive-message-connect-timeout",
					"Receiving a message was unsuccessful because of a connection timeout"
			),
			RECEIVE_MESSAGE_HTTP_HOST_CONNECT(
					"receive-message-http-host-connect",
					"Receiving a message was unsuccessful because the connection was refused"
			),
			RECEIVE_MESSAGE_UNKNOWN_HOST(
					"receive-message-unknown-host",
					"Receiving a message was unsuccessful because the target hostname could not be resolved"
			),

			LOCAL_BINARY_DELETE_TIMEOUT_CONNECT(
					"local-binary-delete-timeout-connect",
					"Local instance encountered a connect timeout trying to clean up the binary resource"
			),
			LOCAL_BINARY_DELETE_TIMEOUT_READ(
					"local-binary-delete-timeout-read",
					"Local instance encountered a read timeout trying to clean up the binary resource"
			),
			LOCAL_BINARY_DELETE_HTTP_HOST_CONNECT(
					"local-binary-delete-http-host-connect",
					"Local instance was unable to clean up the binary resource from the local DSF FHIR server because the connection was refused"
			),
			LOCAL_BINARY_DELETE_HTTP_401(
					"local-binary-delete-http-401",
					"Local instance encountered a HTTP status 401 trying to clean up the binary resource"
			),
			LOCAL_BINARY_DELETE_HTTP_403(
					"local-binary-delete-http-403",
					"Local instance encountered a HTTP status 403 trying to clean up the binary resource"
			),
			LOCAL_BINARY_DELETE_HTTP_500(
					"local-binary-delete-http-500",
					"Local instance encountered a HTTP status 500 trying to clean up the binary resource"
			),
			LOCAL_BINARY_DELETE_HTTP_502(
					"local-binary-delete-http-502",
					"Local instance encountered a HTTP status 502 trying to clean up the binary resource"
			),
			LOCAL_BINARY_DELETE_HTTP_UNEXPECTED(
					"local-binary-delete-http-unexpected",
					"Local instance encountered an unexpected HTTP status code trying to clean up the binary resource"
			),

			REMOTE_BINARY_DELETE_TIMEOUT_CONNECT(
					"remote-binary-delete-timeout-connect",
					"Remote instance encountered a connect timeout trying to clean up the binary resource"
			),
			REMOTE_BINARY_DELETE_TIMEOUT_READ(
					"remote-binary-delete-timeout-read",
					"Remote instance encountered a read timeout trying to clean up the binary resource"
			),
			REMOTE_BINARY_DELETE_HTTP_HOST_CONNECT(
					"remote-binary-delete-http-host-connect",
					"Remote instance was unable to clean up the binary resource from its DSF FHIR server because the connection was refused"
			),
			REMOTE_BINARY_DELETE_HTTP_401(
					"remote-binary-delete-http-401",
					"Remote instance encountered a HTTP status 401 trying to clean up the binary resource"
			),
			REMOTE_BINARY_DELETE_HTTP_403(
					"remote-binary-delete-http-403",
					"Remote instance encountered a HTTP status 403 trying to clean up the binary resource"
			),
			REMOTE_BINARY_DELETE_HTTP_500(
					"remote-binary-delete-http-500",
					"Remote instance encountered a HTTP status 500 trying to clean up the binary resource"
			),
			REMOTE_BINARY_DELETE_HTTP_502(
					"remote-binary-delete-http-502",
					"Remote instance encountered a HTTP status 502 trying to clean up the binary resource"
			),
			REMOTE_BINARY_DELETE_HTTP_UNEXPECTED(
					"remote-binary-delete-http-unexpected",
					"Remote instance encountered an unexpected HTTP status code trying to clean up the binary resource"
			),

			LOCAL_BINARY_POST_HTTP_401(
					"local-binary-post-http-401",
					"Local instance encountered a HTTP status 401 trying to post the binary resource to its own FHIR server"
			),
			LOCAL_BINARY_POST_HTTP_403(
					"local-binary-post-http-403",
					"Local instance encountered a HTTP status 403 trying to post the binary resource to its own FHIR server"
			),
			LOCAL_BINARY_POST_HTTP_413(
					"local-binary-post-http-413",
					"Local instance encountered a HTTP status 413 trying to post the binary resource to its own FHIR server"
			),
			LOCAL_BINARY_POST_HTTP_500(
					"local-binary-post-http-500",
					"Local instance encountered a HTTP status 500 trying to post the binary resource to its own FHIR server"
			),
			LOCAL_BINARY_POST_HTTP_502(
					"local-binary-post-http-502",
					"Local instance encountered a HTTP status 502 trying to post the binary resource to its own FHIR server"
			),
			LOCAL_BINARY_POST_HTTP_UNEXPECTED(
					"local-binary-post-http-unexpected",
					"Local instance encountered an unexpected HTTP status code trying to post the binary resource to its own FHIR server"
			),
			LOCAL_BINARY_POST_TIMEOUT_CONNECT(
					"local-binary-post-timeout-connect",
					"Local instance encountered a connect timeout trying to post the binary resource to its own FHIR server"
			),
			LOCAL_BINARY_POST_TIMEOUT_READ(
					"local-binary-post-timeout-read",
					"Local instance encountered a read timeout trying to post the binary resource to its own FHIR server"
			),
			LOCAL_BINARY_POST_HTTP_HOST_CONNECT(
					"local-binary-post-http-host-connect",
					"Local instance was unable to post the binary resource to its own DSF FHIR server because the connection was refused"
			),

			REMOTE_BINARY_POST_HTTP_401(
					"remote-binary-post-http-401",
					"Remote instance encountered a HTTP status 401 trying to post the binary resource to its own FHIR server"
			),
			REMOTE_BINARY_POST_HTTP_403(
					"remote-binary-post-http-403",
					"Remote instance encountered a HTTP status 403 trying to post the binary resource to its own FHIR server"
			),
			REMOTE_BINARY_POST_HTTP_413(
					"remote-binary-post-http-413",
					"Remote instance encountered a HTTP status 413 trying to post the binary resource to its own FHIR server"
			),
			REMOTE_BINARY_POST_HTTP_500(
					"remote-binary-post-http-500",
					"Remote instance encountered a HTTP status 500 trying to post the binary resource to its own FHIR server"
			),
			REMOTE_BINARY_POST_HTTP_502(
					"remote-binary-post-http-502",
					"Remote instance encountered a HTTP status 502 trying to post the binary resource to its own FHIR server"
			),
			REMOTE_BINARY_POST_HTTP_UNEXPECTED(
					"remote-binary-post-http-unexpected",
					"Remote instance encountered an unexpected HTTP status code trying to post the binary resource to its own FHIR server"
			),
			REMOTE_BINARY_POST_TIMEOUT_CONNECT(
					"remote-binary-post-timeout-connect",
					"Remote instance encountered a connect timeout trying to post the binary resource to its own FHIR server"
			),
			REMOTE_BINARY_POST_TIMEOUT_READ(
					"remote-binary-post-timeout-read",
					"Remote instance encountered a read timeout trying to post the binary resource to its own FHIR server"
			),
			REMOTE_BINARY_POST_HTTP_HOST_CONNECT(
					"remote-binary-post-http-host-connect",
					"Remote instance was unable to post the binary resource to its own DSF FHIR server because the connection was refused"
			),

			RESPONSE_MESSAGE_TIMEOUT_STATUS_REQUESTED(
					"response-message-timeout-status-requested",
					"Response message timed out. The status of the request on the target FHIR server is 'requested'"
			),
			RESPONSE_MESSAGE_TIMEOUT_STATUS_IN_PROGRESS(
					"response-message-timeout-status-in-progress",
					"Response message timed out. The status of the request on the target FHIR server is 'in-progress'"
			),
			RESPONSE_MESSAGE_TIMEOUT_STATUS_FAILED(
					"response-message-timeout-status-failed",
					"Response message timed out. The status of the request on the target FHIR server is 'failed'"
			),
			RESPONSE_MESSAGE_TIMEOUT_STATUS_COMPLETED(
					"response-message-timeout-status-completed",
					"Response message timed out. The status of the request on the target FHIR server is 'completed'"
			),
			RESPONSE_MESSAGE_TIMEOUT_STATUS_UNEXPECTED(
					"response-message-timeout-status-unexpected",
					"Response message timed out. The status of the request on the target FHIR server is neither of 'requested', 'in-progress', 'failed' or 'completed'"
			),

			RESPONSE_MESSAGE_TIMEOUT_HTTP_401(
					"response-message-timeout-http-401",
					"Response message timed out. Received HTTP status 401 trying to check request status on the target"
			),
			RESPONSE_MESSAGE_TIMEOUT_HTTP_403(
					"response-message-timeout-http-403",
					"Response message timed out. Received HTTP status 403 trying to check request status on the target"
			),
			RESPONSE_MESSAGE_TIMEOUT_HTTP_500(
					"response-message-timeout-http-500",
					"Response message timed out. Received HTTP status 500 trying to check request status on the target"
			),
			RESPONSE_MESSAGE_TIMEOUT_HTTP_502(
					"response-message-timeout-http-502",
					"Response message timed out. Received HTTP status 502 trying to check request status on the target"
			),
			RESPONSE_MESSAGE_TIMEOUT_HTTP_UNEXPECTED(
					"response-message-timeout-http-unexpected",
					"Response message timed out. Received an unexpected HTTP status code trying to check request status on the target"
			),

			CLEANUP_MESSAGE_TIMEOUT(
					"cleanup-message-timeout",
					"Timeout while waiting for cleanup message from remote instance"
			),

			LOCAL_BINARY_DOWNLOAD_IO_ERROR(
					"local-binary-download-io-error",
					"Local instance encountered an I/O error trying to download the binary resource from the target"
			),
			LOCAL_BINARY_DOWNLOAD_HTTP_401(
					"local-binary-download-http-401",
					"Local instance received HTTP status 401 trying to download the binary resource from the target"
			),
			LOCAL_BINARY_DOWNLOAD_HTTP_403(
					"local-binary-download-http-403",
					"Local instance received HTTP status 403 trying to download the binary resource from the target"
			),
			LOCAL_BINARY_DOWNLOAD_HTTP_500(
					"local-binary-download-http-500",
					"Local instance received HTTP status 500 trying to download the binary resource from the target"
			),
			LOCAL_BINARY_DOWNLOAD_HTTP_502(
					"local-binary-download-http-502",
					"Local instance received HTTP status 500 trying to download the binary resource from the target"
			),
			LOCAL_BINARY_DOWNLOAD_HTTP_UNEXPECTED(
					"local-binary-download-http-unexpected",
					"Local instance received an unexpected HTTP status trying to download the binary resource from the target"
			),
			LOCAL_BINARY_DOWNLOAD_TIMEOUT_CONNECT(
					"local-binary-download-timeout-connect",
					"Local instance encountered a connect timeout trying to download the binary resource from the target"
			),
			LOCAL_BINARY_DOWNLOAD_TIMEOUT_READ(
					"local-binary-download-timeout-read",
					"Local instance encountered a read timeout trying to download the binary resource from the target"
			),
			LOCAL_BINARY_DOWNLOAD_HTTP_HOST_CONNECT(
					"local-binary-download-http-host-connect",
					"Local instance was unable to download the binary resource from the remote DSF FHIR server because the connection was refused"
			),
			LOCAL_BINARY_DOWNLOAD_MISSING_REFERENCE(
					"local-binary-download-missing-reference",
					"Local instance was unable to download the binary resource from the target because the reference was missing"
			),

			REMOTE_BINARY_DOWNLOAD_IO_ERROR(
					"remote-binary-download-io-error",
					"Remote instance encountered an I/O error trying to download the binary resource from this server"
			),
			REMOTE_BINARY_DOWNLOAD_HTTP_401(
					"remote-binary-download-http-401",
					"Remote instance received HTTP status 401 trying to download the binary resource from this server"
			),
			REMOTE_BINARY_DOWNLOAD_HTTP_403(
					"remote-binary-download-http-403",
					"Remote instance received HTTP status 403 trying to download the binary resource from this server"
			),
			REMOTE_BINARY_DOWNLOAD_HTTP_500(
					"remote-binary-download-http-500",
					"Remote instance received HTTP status 500 trying to download the binary resource from this server"
			),
			REMOTE_BINARY_DOWNLOAD_HTTP_502(
					"remote-binary-download-http-502",
					"Remote instance received HTTP status 502 trying to download the binary resource from this server"
			),
			REMOTE_BINARY_DOWNLOAD_HTTP_UNEXPECTED(
					"remote-binary-download-http-unexpected",
					"Remote instance received an unexpected HTTP status trying to download the binary resource from this server"
			),
			REMOTE_BINARY_DOWNLOAD_TIMEOUT_CONNECT(
					"remote-binary-download-timeout-connect",
					"Remote instance encountered a connect timeout trying to download the binary resource from this server"
			),
			REMOTE_BINARY_DOWNLOAD_TIMEOUT_READ(
					"remote-binary-download-timeout-read",
					"Remote instance encountered a read timeout trying to download the binary resource from this server"
			),
			REMOTE_BINARY_DOWNLOAD_HTTP_HOST_CONNECT(
					"remote-binary-download-timeout-read",
					"Remote instance encountered a read timeout trying to download the binary resource from this server"
			),
			REMOTE_BINARY_DOWNLOAD_MISSING_REFERENCE(
					"remote-binary-download-http-host-connect",
					"Remote instance was unable to download the binary resource from the local DSF FHIR server because the connection was refused"
			),

			LOCAL_UNKNOWN(
					"local-unknown",
					"An unknown error was encountered by the local instance"
			),
			REMOTE_UNKNOWN(
					"remote-unknown",
					"An unknown error was encountered by the remote instance"
			);

			private final String code;
			private final String display;

			private static final Map<String, Concept> CODE_TO_ENUM = new HashMap<>();

			static
			{
				for (Concept e : values())
				{
					CODE_TO_ENUM.put(e.code, e);
				}
			}

			Concept(String code, String display)
			{
				this.code = code;
				this.display = display;
			}

			public String getCode()
			{
				return code;
			}

			public String getDisplay()
			{
				return display;
			}

			public static Concept fromCode(String code)
			{

				return CODE_TO_ENUM.get(code);
			}
		}
	}
}
