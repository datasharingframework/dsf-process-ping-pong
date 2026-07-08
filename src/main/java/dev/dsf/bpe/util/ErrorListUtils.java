package dev.dsf.bpe.util;

import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.ProcessErrors;
import dev.dsf.bpe.v2.variables.Variables;

public class ErrorListUtils
{
	public static void addAll(ProcessErrors errors, Variables variables)
	{
		ProcessErrors errorList = getErrorList(variables);
		if (errors != null)
		{
			errorList.addAll(errors);
			saveErrorList(errorList, variables, null);
		}
	}

	public static void addAll(ProcessErrors errors, Variables variables, String correlationKey)
	{
		ProcessErrors errorList = correlationKey != null ? getErrorList(variables, correlationKey)
				: getErrorList(variables);
		if (errors != null)
		{
			errorList.addAll(errors);
			saveErrorList(errorList, variables, correlationKey);
		}
	}

	public static void addAllRemote(ProcessErrors errors, Variables variables)
	{
		ProcessErrors errorList = getErrorListRemote(variables);
		if (errors != null)
		{
			errorList.addAll(errors);
			saveErrorListRemote(errorList, variables, null);
		}
	}

	public static void addAllRemote(ProcessErrors errors, Variables variables, String correlationKey)
	{
		ProcessErrors errorList = correlationKey != null ? getErrorListRemote(variables, correlationKey)
				: getErrorListRemote(variables);
		if (errors != null)
		{
			errorList.addAll(errors);
			saveErrorListRemote(errorList, variables, correlationKey);
		}
	}

	public static void add(ProcessError error, Variables variables)
	{
		add(error, variables, null);
	}

	public static void add(ProcessError error, Variables variables, String correlationKey)
	{
		if (correlationKey != null)
		{
			add(error, ExecutionVariables.errors.correlatedValue(correlationKey), variables);
		}
		else
		{
			add(error, ExecutionVariables.errors.name(), variables);
		}
	}

	public static void addRemote(ProcessError error, Variables variables)
	{
		addRemote(error, variables, null);
	}

	public static void addRemote(ProcessError error, Variables variables, String correlationKey)
	{
		if (correlationKey != null)
		{
			add(error, ExecutionVariables.errorsRemote.correlatedValue(correlationKey), variables);
		}
		else
		{
			add(error, ExecutionVariables.errorsRemote.name(), variables);
		}
	}

	public static ProcessErrors getErrorList(Variables variables)
	{
		return getErrorList(variables, null);
	}

	public static ProcessErrors getErrorList(Variables variables, String correlationKey)
	{
		if (correlationKey != null)
		{
			return getErrorList(ExecutionVariables.errors.correlatedValue(correlationKey), variables);
		}
		else
		{
			return getErrorList(ExecutionVariables.errors.name(), variables);
		}
	}

	public static ProcessErrors getErrorListRemote(Variables variables)
	{
		return getErrorListRemote(variables, null);
	}

	public static ProcessErrors getErrorListRemote(Variables variables, String correlationKey)
	{
		if (correlationKey != null)
		{
			return getErrorList(ExecutionVariables.errorsRemote.correlatedValue(correlationKey), variables);
		}
		else
		{
			return getErrorList(ExecutionVariables.errorsRemote.name(), variables);
		}
	}

	public static ProcessErrors getErrorList(String variableName, Variables variables)
	{
		ProcessErrors errors = variables.getVariable(variableName);
		if (errors == null)
		{
			errors = new ProcessErrors();
			saveErrorList(errors, variableName, variables);
		}
		return errors;
	}

	public static void add(ProcessError error, String variableName, Variables variables)
	{
		ProcessErrors errors = getErrorList(variableName, variables);
		errors.add(error);
		saveErrorList(errors, variableName, variables);
	}

	private static void saveErrorList(ProcessErrors errors, Variables variables, String correlationKey)
	{
		if (correlationKey != null)
		{
			saveErrorList(errors, ExecutionVariables.errors.correlatedValue(correlationKey), variables);
		}
		else
		{
			saveErrorList(errors, ExecutionVariables.errors.name(), variables);
		}
	}

	private static void saveErrorListRemote(ProcessErrors errors, Variables variables, String correlationKey)
	{
		if (correlationKey != null)
		{
			saveErrorList(errors, ExecutionVariables.errorsRemote.correlatedValue(correlationKey), variables);
		}
		else
		{
			saveErrorList(errors, ExecutionVariables.errorsRemote.name(), variables);
		}
	}

	private static void saveErrorList(ProcessErrors errors, String variableName, Variables variables)
	{
		variables.setJsonVariable(variableName, errors);
	}
}
