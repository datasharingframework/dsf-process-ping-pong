package dev.dsf.bpe.util.logging;

import org.hl7.fhir.r4.model.Task;

public class TaskIdPrefixer
{
	private TaskIdPrefixer()
	{

	}

	public static String prefixTaskId(Task task, String message)
	{
		return "Process for task with id " + task.getIdElement().getValue() + ": " + message;
	}
}
