package dev.dsf.bpe.service.ping;

import static dev.dsf.bpe.ConstantsPing.POTENTIAL_FIX_URL_REMOTE_ORG_FHIR_SERVER_REFERENCE_RESOLUTION;

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

public class SaveReferenceResolutionErrorPing implements ServiceTask
{
	private static final Logger logger = LoggerFactory.getLogger(SaveReferenceResolutionErrorPing.class);

	@Override
	public void execute(ProcessPluginApi processPluginApi, Variables variables) throws ErrorBoundaryEvent, Exception
	{
		String correlationKey = variables.getTarget().getCorrelationKey();
		ProcessError error = new ProcessError(ConstantsPing.PROCESS_NAME_PING,
				CodeSystem.DsfPingError.Concept.REMOTE_ORG_FHIR_SERVER_REFERENCE_RESOLUTION,
				POTENTIAL_FIX_URL_REMOTE_ORG_FHIR_SERVER_REFERENCE_RESOLUTION);

		ErrorListUtils.add(error, variables, correlationKey);
		variables.setJsonVariable(ExecutionVariables.statusCode.correlatedValue(correlationKey),
				CodeSystem.DsfPingStatus.Code.PONG_MISSING);
		logger.debug("Saved error when trying to send second ping message without reference. Error message: {}",
				error.concept().getDisplay());
	}
}
