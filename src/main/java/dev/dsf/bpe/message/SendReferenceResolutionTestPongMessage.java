package dev.dsf.bpe.message;

import java.util.List;

import org.hl7.fhir.r4.model.Task;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.util.task.SendTaskErrorConverter;
import dev.dsf.bpe.util.task.input.generator.TestReferenceGenerator;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.MessageSendTask;
import dev.dsf.bpe.v2.activity.values.SendTaskValues;
import dev.dsf.bpe.v2.error.MessageSendTaskErrorHandler;
import dev.dsf.bpe.v2.error.impl.DefaultMessageSendTaskErrorHandler;
import dev.dsf.bpe.v2.variables.Target;
import dev.dsf.bpe.v2.variables.Variables;

public class SendReferenceResolutionTestPongMessage implements MessageSendTask
{
	@Override
	public List<Task.ParameterComponent> getAdditionalInputParameters(ProcessPluginApi api, Variables variables,
			SendTaskValues sendTaskValues, Target target)
	{
		String testReference = variables.getString(ExecutionVariables.downloadResourceReference.name());
		Task.ParameterComponent testReferenceInput = TestReferenceGenerator.create(testReference,
				api.getProcessPluginDefinition().getResourceVersion());

		return List.of(testReferenceInput);
	}

	@Override
	public MessageSendTaskErrorHandler getErrorHandler()
	{
		return new DefaultMessageSendTaskErrorHandler()
		{
			@Override
			public Exception handleException(ProcessPluginApi api, Variables variables, SendTaskValues sendTaskValues,
					Exception exception)
			{
				SendTaskErrorConverter.ProcessErrorWithStatusCode errorAndStatusLocal = SendTaskErrorConverter
						.convertLocal(exception, true, ConstantsPing.PROCESS_NAME_PONG);

				CodeSystem.DsfPingStatus.Code statusCode = errorAndStatusLocal.statusCode();
				variables.setJsonVariable(ExecutionVariables.statusCode.name(), statusCode);

				String httpStatus = errorAndStatusLocal.rawHttpStatus();
				variables.setString(ExecutionVariables.rawHttpStatus.name(), httpStatus);

				variables.setJsonVariable(ExecutionVariables.errorLocal.name(), errorAndStatusLocal.error());


				SendTaskErrorConverter.ProcessErrorWithStatusCode errorAndStatusRemote = SendTaskErrorConverter
						.convertLocal(exception, false, ConstantsPing.PROCESS_NAME_PONG);

				variables.setJsonVariable(ExecutionVariables.errorRemote.name(), errorAndStatusRemote.error());

				return null;
			}
		};
	}
}
