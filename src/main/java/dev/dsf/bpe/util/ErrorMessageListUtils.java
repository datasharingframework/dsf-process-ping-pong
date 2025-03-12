package dev.dsf.bpe.util;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import dev.dsf.bpe.ConstantsPing;

public class ErrorMessageListUtils
{
	@SuppressWarnings("unchecked")
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
			errorMessages = new ArrayList<>();
			execution.setVariable(variableName, errorMessages);
		}
		errorMessages.add(error);
		return errorMessages;
	}
}
