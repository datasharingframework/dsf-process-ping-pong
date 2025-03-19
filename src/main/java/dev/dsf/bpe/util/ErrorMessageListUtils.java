package dev.dsf.bpe.util;

import java.util.List;
import java.util.Vector;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ConstantsPing;

public class ErrorMessageListUtils
{
	public static List<String> addAll(List<String> errors, DelegateExecution execution)
	{
		List<String> errorList = getErrorMessageList(execution);
		if (errors == null)
			return errorList;
		errorList.addAll(errors);
		return errorList;
	}

	public static List<String> addAll(List<String> errors, DelegateExecution execution, String correlationKey)
	{
		List<String> errorList = correlationKey != null ? getErrorMessageList(execution, correlationKey) : getErrorMessageList(execution);
		if (errors == null)
			return errorList;
		errorList.addAll(errors);
		return errorList;
	}

	public static List<String> add(String error, DelegateExecution execution)
	{
		return add(error, execution, null);
	}

	public static List<String> add(String error, DelegateExecution execution, String correlationKey)
	{
		if (correlationKey != null)
		{
			return add(error, ConstantsPing.getBpmnExecutionVariableErrorMessageList(correlationKey), execution);
		} else
		{
			return add(error, ConstantsPing.getBpmnExecutionVariableErrorMessageList(), execution);
		}
	}

	public static List<String> getErrorMessageList(DelegateExecution execution)
	{
		return getErrorMessageList(execution, null);
	}

	@SuppressWarnings("unchecked")
	public static List<String> getErrorMessageList(DelegateExecution execution, String correlationKey)
	{
		List<String> errorMessages = correlationKey != null ?(List<String>) execution
				.getVariable(ConstantsPing.getBpmnExecutionVariableErrorMessageList(correlationKey)) :(List<String>) execution
				.getVariable(ConstantsPing.getBpmnExecutionVariableErrorMessageList());
		if (errorMessages == null)
		{
			errorMessages = new Vector<>();
			if (correlationKey != null)
			{
				execution.setVariable(ConstantsPing.getBpmnExecutionVariableErrorMessageList(correlationKey), errorMessages);
			} else
			{
				execution.setVariable(ConstantsPing.getBpmnExecutionVariableErrorMessageList(), errorMessages);
			}
		}
		return errorMessages;
	}

	@SuppressWarnings("unchecked")
	public static List<String> add(String error, String variableName, DelegateExecution execution)
	{
		List<String> errorMessages = (List<String>) execution.getVariable(variableName);
		if (errorMessages == null)
		{
			errorMessages = new Vector<>();
			execution.setVariable(variableName, errorMessages);
		}
		errorMessages.add(error);
		return errorMessages;
	}
}
