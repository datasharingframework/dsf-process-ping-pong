package dev.dsf.bpe.util;

import java.util.List;
import java.util.Vector;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;

public class ErrorListUtils
{
	public static void addAll(List<ProcessError> errors, DelegateExecution execution)
	{
		List<ProcessError> errorList = getErrorMessageList(execution);
		if (errors != null)
		{
			errorList.addAll(errors);
			saveErrorList(errorList, execution, null);
		}
	}

	public static void addAll(List<ProcessError> errors, DelegateExecution execution, String correlationKey)
	{
		List<ProcessError> errorList = correlationKey != null ? getErrorMessageList(execution, correlationKey)
				: getErrorMessageList(execution);
		if (errors != null)
		{
			errorList.addAll(errors);
			saveErrorList(errorList, execution, correlationKey);
		}
	}

	public static void add(ProcessError error, DelegateExecution execution)
	{
		add(error, execution, null);
	}

	public static void add(ProcessError error, DelegateExecution execution, String correlationKey)
	{
		if (correlationKey != null)
		{
			add(error, ExecutionVariables.ERROR_LIST.correlatedValue(correlationKey), execution);
		}
		else
		{
			add(error, ExecutionVariables.ERROR_LIST.getValue(), execution);
		}
	}

	public static List<ProcessError> getErrorMessageList(DelegateExecution execution)
	{
		return getErrorMessageList(execution, null);
	}

	public static List<ProcessError> getErrorMessageList(DelegateExecution execution, String correlationKey)
	{
		if (correlationKey != null)
		{
			return getErrorMessageList(ExecutionVariables.ERROR_LIST.correlatedValue(correlationKey), execution);
		}
		else
		{
			return getErrorMessageList(ExecutionVariables.ERROR_LIST.getValue(), execution);
		}
	}

	public static List<ProcessError> getErrorMessageList(String variableName, DelegateExecution execution)
	{
		String errorJson = (String) execution.getVariable(variableName);
		if (errorJson == null)
		{
			List<ProcessError> errors = new Vector<>();
			execution.setVariable(variableName, ProcessError.toString(errors));
			return errors;
		}
		else
		{
			return ProcessError.parseList(errorJson);
		}
	}

	public static void add(ProcessError error, String variableName, DelegateExecution execution)
	{
		List<ProcessError> errors = getErrorMessageList(variableName, execution);
		errors.add(error);
		saveErrorList(errors, variableName, execution);
	}

	private static void saveErrorList(List<ProcessError> errors, DelegateExecution execution, String correlationKey)
	{
		if (correlationKey != null)
		{
			saveErrorList(errors, ExecutionVariables.ERROR_LIST.correlatedValue(correlationKey), execution);
		}
		else
		{
			saveErrorList(errors, ExecutionVariables.ERROR_LIST.getValue(), execution);
		}
	}

	private static void saveErrorList(List<ProcessError> errors, String variableName, DelegateExecution execution)
	{
		execution.setVariable(variableName, ProcessError.toString(errors));
	}
}
