package dev.dsf.bpe.listener;

import java.util.Objects;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ExecutionListener;
import dev.dsf.bpe.v2.variables.Variables;

public class PingStatusGatewayListener implements ExecutionListener
{
	@Override
	public void notify(ProcessPluginApi api, Variables variables) throws Exception
	{
		setPingStatusAsString(variables);
	}

	private void setPingStatusAsString(Variables variables)
	{
		CodeSystem.DsfPingStatus.Code statusCode = variables.getVariableLocal(ExecutionVariables.statusCode.name());
		if (Objects.nonNull(statusCode))
		{
			variables.setStringLocal(ExecutionVariables.statusCodeString.name(), statusCode.getValue());
		}
		else
		{
			variables.setStringLocal(ExecutionVariables.statusCodeString.name(), null);
		}
	}
}
