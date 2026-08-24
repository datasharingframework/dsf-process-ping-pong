package dev.dsf.bpe.service.pong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.dsf.bpe.CodeSystem;
import dev.dsf.bpe.ConstantsPing;
import dev.dsf.bpe.ExecutionVariables;
import dev.dsf.bpe.ProcessError;
import dev.dsf.bpe.util.ErrorListUtils;
import dev.dsf.bpe.v2.ProcessPluginApi;
import dev.dsf.bpe.v2.activity.ServiceTask;
import dev.dsf.bpe.v2.error.ErrorBoundaryEvent;
import dev.dsf.bpe.v2.variables.Variables;

public class SaveReferenceResolutionErrorPong implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SaveReferenceResolutionErrorPong.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		String correlationKey = variables.getTarget().getCorrelationKey();
		ProcessError errorLocal = new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
				CodeSystem.DsfPingError.Concept.REMOTE_ORG_FHIR_SERVER_REFERENCE_RESOLUTION,
				ConstantsPing.POTENTIAL_FIX_URL_REMOTE_ORG_FHIR_SERVER_REFERENCE_RESOLUTION);
		ErrorListUtils.add(errorLocal, variables, correlationKey);

		ProcessError errorRemote = new ProcessError(ConstantsPing.PROCESS_NAME_PONG,
				CodeSystem.DsfPingError.Concept.LOCAL_ORG_FHIR_SERVER_REFERENCE_RESOLUTION,
				ConstantsPing.POTENTIAL_FIX_URL_LOCAL_ORG_FHIR_SERVER_REFERENCE_RESOLUTION);
		ErrorListUtils.addRemote(errorRemote, variables);

		variables.setBoolean(ExecutionVariables.sendPong.name(), true);
		variables.setBoolean(ExecutionVariables.includeReferencePong.name(), false);

		logger.info(
				"Sending a message with a reference failed with HTTP 403, sending a message without a reference succeeded: {}",
				errorLocal.concept().getDisplay());
	}
}
