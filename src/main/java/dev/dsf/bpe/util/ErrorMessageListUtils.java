package dev.dsf.bpe.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ConstantsPing;

public class ErrorMessageListUtils
{
	public static List<String> addAll(List<String> errors, DelegateExecution execution)
	{
		List<String> errorList = getErrorMessageList(execution);
		errorList.addAll(errors);
		return errorList;
	}

	public static List<String> add(String error, DelegateExecution execution)
	{
		return add(error, ConstantsPing.BPMN_EXECUTION_VARIABLE_ERROR_MESSAGE_LIST, execution);
	}

	@SuppressWarnings("unchecked")
	public static List<String> getErrorMessageList(DelegateExecution execution)
	{
		return (List<String>) execution.getVariable(ConstantsPing.BPMN_EXECUTION_VARIABLE_ERROR_MESSAGE_LIST);
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
