package dev.dsf.bpe;

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
		public static final String URL = "http://dsf.dev/fhir/CodeSystem/ping-v2";

		private DsfPing()
		{
		}

		public enum Code implements SingleStringValueEnum
		{
			PING_STATUS("ping-status"),
			PONG_STATUS("pong-status"),
			ENDPOINT_IDENTIFIER("endpoint-identifier"),
			TARGET_ENDPOINTS("target-endpoints"),
			TIMER_INTERVAL("timer-interval"),
			DOWNLOAD_RESOURCE_SIZE_BYTES("download-resource-size-bytes"),
			DOWNLOADED_DURATION_MILLIS("downloaded-duration-millis"),
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
		public static final String URL = "http://dsf.dev/fhir/CodeSystem/ping-status-v2";

		private DsfPingStatus()
		{
		}

		public enum Code implements SingleStringValueEnum
		{
			COMPLETED("completed"),
			PENDING("pending"),
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

	public static final class DsfPingProcesses
	{
		public static final String URL = "http://dsf.dev/fhir/CodeSystem/ping-processes-v2";

		private DsfPingProcesses()
		{
		}

		public enum Code implements SingleStringValueEnum
		{
			PING("ping"),
			PONG("pong");

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

	public static final class DsfPingProcessSteps
	{
		public static final String URL = "http://dsf.dev/fhir/CodeSystem/ping-process-steps-v2";

		private DsfPingProcessSteps()
		{
		}

		public enum Code implements SingleStringValueEnum
		{
			SET_DOWNLOAD_RESOURCE_SIZE("set-download-resource-size"),
			GENERATE_AND_STORE_RESOURCE("generate-and-store-resource"),
			LOG_AND_SAVE_ERROR("log-and-save-error"),
			SELECT_TARGETS("select-targets"),
			PING("ping"),
			LOG_AND_SAVE_SEND_ERROR("log-and-save-send-error"),
			LOG_AND_SAVE_NO_RESPONSE("log-and-save-no-response"),
			SAVE_PONG("save-pong"),
			DOWNLOAD_RESOURCE_AND_MEASURE_SPEED("download-resource-and-measure-speed"),
			CLEANUP_PONG("cleanup-pong"),
			CLEANUP("cleanup"),
			STORE_RESULTS("store-results"),
			LOG_PING("log-ping"),
			SET_ENDPOINT_IDENTIFIER("set-endpoint-identifier"),
			SELECT_PONG_TARGET("select-pong-target"),
			STORE_DOWNLOAD_SPEED("store-download-speed"),
			LOG_AND_SAVE_AND_STORE_ERROR("log-and-save-and-store-error"),
			ESTIMATE_CLEANUP_TIMER_DURATION("estimate-cleanup-timer-duration"),
			PONG("pong"),
			STORE_UPLOAD_SPEED("store-upload-speed"),
			SAVE_TIMEOUT_ERROR("save-timeout-error"),
			STORE_ERRORS("store-errors"),
			CLEANUP_TIMER_CATCH_EVENT("cleanup-timer-catch-event"),
			PONG_MESSAGE_TIMEOUT_TIMER_CATCH_EVENT("pong-timer-catch-event"),
			CHECK_PING_TASK_STATUS("check-ping-task-status");

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

	public static final class DsfPingUnits
	{
		public static final String URL = "http://dsf.dev/fhir/CodeSystem/ping-units-v2";

		private DsfPingUnits()
		{
		}

		public enum Code implements SingleStringValueEnum
		{
			BITS_PER_SECOND("bits-per-second"),
			BYTES_PER_SECOND("bytes-per-second"),
			MEGABITS_PER_SECOND("megabits-per-second"),
			MEGABYTES_PER_SECOND("megabytes-per-second");

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

	public static final class ReadAccessTag
	{
		public static final String URL = "http://dsf.dev/fhir/CodeSystem/read-access-tag";

		private ReadAccessTag()
		{
		}

		public enum Code implements SingleStringValueEnum
		{
			ALL("ALL"),
			LOCAL("LOCAL"),
			ORGANIZATION("ORGANIZATION"),
			ROLE("ROLE");

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
}
