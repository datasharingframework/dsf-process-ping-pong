package dev.dsf.bpe.service.ping;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.service.AbstractService;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;

public class SetPongTimeoutDuration extends AbstractService
{
	private static final Logger logger = LoggerFactory.getLogger(SetPongTimeoutDuration.class);

	public SetPongTimeoutDuration(ProcessPluginApi api)
	{
		super(api);
	}

	@Override
	protected void doExecuteWithErrorHandling(DelegateExecution execution, Variables variables) throws Exception
	{
		logger.debug("Setting pong timer duration...");
		Task startTask = variables.getStartTask();
		Optional<String> optPongTimeoutDuration = api.getTaskHelper().getFirstInputParameterStringValue(startTask,
				CodeSystem.DsfPing.URL, CodeSystem.DsfPing.Code.PONG_TIMEOUT_DURATION_ISO_8601.getValue());
		if (optPongTimeoutDuration.isPresent())
		{
			String pongTimeoutDuration = optPongTimeoutDuration.get();
			if (isDurationValid(pongTimeoutDuration))
			{
				variables.setString(ExecutionVariables.pongTimerDuration.name(), pongTimeoutDuration);
				logger.debug("Pong timer duration set to {}", pongTimeoutDuration);
			}
			else
			{
				throw new RuntimeException("Pong timeout duration is invalid");
			}
		}
		else
		{
			logger.debug("No pong timeout duration specified");
		}
	}

	private boolean isDurationValid(String duration)
	{
		try
		{
			Duration.parse(duration);
			return true;
		}
		catch (DateTimeParseException e)
		{
			return false;
		}
	}
}
