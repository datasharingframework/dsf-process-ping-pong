package dev.dsf.bpe.util;

import java.util.List;
import java.util.Vector;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.dsf.bpe.ConstantsPing;
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
			add(error, ConstantsPing.getBpmnExecutionVariableErrorMessageList(correlationKey), execution);
		}
		else
		{
			add(error, ConstantsPing.getBpmnExecutionVariableErrorList(), execution);
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
			return getErrorMessageList(ConstantsPing.getBpmnExecutionVariableErrorMessageList(correlationKey),
					execution);
		}
		else
		{
			return getErrorMessageList(ConstantsPing.getBpmnExecutionVariableErrorList(), execution);
		}
	}

	public static List<ProcessError> getErrorMessageList(String variableName, DelegateExecution execution)
	{
		String errorJson = (String) execution.getVariable(variableName);
		if (errorJson == null)
		{
			List<ProcessError> errors = new Vector<>();
			try
			{
				execution.setVariable(variableName, ProcessError.toString(errors));
			}
			catch (JsonProcessingException e)
			{
				throw new RuntimeException(e);
			}
			return errors;
		}
		else
		{
			try
			{
				return ProcessError.parseList(errorJson);
			}
			catch (JsonProcessingException e)
			{
				throw new RuntimeException(e);
			}
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
			saveErrorList(errors, ConstantsPing.getBpmnExecutionVariableErrorMessageList(correlationKey), execution);
		}
		else
		{
			saveErrorList(errors, ConstantsPing.getBpmnExecutionVariableErrorList(), execution);
		}
	}

	private static void saveErrorList(List<ProcessError> errors, String variableName, DelegateExecution execution)
	{
		try
		{
			execution.setVariable(variableName, ProcessError.toString(errors));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}
}
