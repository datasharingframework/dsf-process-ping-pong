package dev.dsf.bpe.util.logging;

import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingPongLogger
{
	private final Logger logger;
	private Task task;

	public PingPongLogger(Class<?> clazz, Task task)
	{
		this.logger = LoggerFactory.getLogger(clazz);
		this.task = task;
	}

	public void info(String message, Object... args)
	{
		logger.info(prependProcessInfo(message), args);
	}

	public void warn(String message, Object... args)
	{
		logger.warn(prependProcessInfo(message), args);
	}

	public void error(String message, Object... args)
	{
		logger.error(prependProcessInfo(message), args);
	}

	public void debug(String message, Object... args)
	{
		logger.debug(prependProcessInfo(message), args);
	}

	private String prependProcessInfo(String message)
	{
		return "Process for Task " + task.getIdElement().getValue() + ": " + message;
	}
}
