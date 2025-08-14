package dev.dsf.bpe.util;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.ProcessErrors;
import dev.dsf.bpe.variables.process_errors.ProcessErrorsValueImpl;

public class ErrorListUtils
{
	public static void addAll(ProcessErrors errors, DelegateExecution execution)
	{
		ProcessErrors errorList = getErrorList(execution);
		if (errors != null)
		{
			errorList.addAll(errors);
			saveErrorList(errorList, execution, null);
		}
	}

	public static void addAll(ProcessErrors errors, DelegateExecution execution, String correlationKey)
	{
		ProcessErrors errorList = correlationKey != null ? getErrorList(execution, correlationKey)
				: getErrorList(execution);
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

	public static ProcessErrors getErrorList(DelegateExecution execution)
	{
		return getErrorList(execution, null);
	}

	public static ProcessErrors getErrorList(DelegateExecution execution, String correlationKey)
	{
		if (correlationKey != null)
		{
			return getErrorList(ExecutionVariables.ERROR_LIST.correlatedValue(correlationKey), execution);
		}
		else
		{
			return getErrorList(ExecutionVariables.ERROR_LIST.getValue(), execution);
		}
	}

	public static ProcessErrors getErrorList(String variableName, DelegateExecution execution)
	{
		ProcessErrors errors = (ProcessErrors) execution.getVariable(variableName);
		if (errors == null)
		{
			errors = new ProcessErrors();
			saveErrorList(errors, variableName, execution);
		}
		return errors;
	}

	public static void add(ProcessError error, String variableName, DelegateExecution execution)
	{
		ProcessErrors errors = getErrorList(variableName, execution);
		errors.add(error);
		saveErrorList(errors, variableName, execution);
	}

	private static void saveErrorList(ProcessErrors errors, DelegateExecution execution, String correlationKey)
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

	private static void saveErrorList(ProcessErrors errors, String variableName, DelegateExecution execution)
	{
		execution.setVariable(variableName, new ProcessErrorsValueImpl(errors));
	}
}
