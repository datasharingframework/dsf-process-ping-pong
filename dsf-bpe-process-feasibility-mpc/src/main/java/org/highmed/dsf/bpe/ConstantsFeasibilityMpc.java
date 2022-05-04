package org.highmed.dsf.bpe;

import static org.highmed.dsf.bpe.ConstantsBase.PROCESS_HIGHMED_URI_BASE;
import static org.highmed.dsf.bpe.FeasibilityMpcProcessPluginDefinition.VERSION;

public interface ConstantsFeasibilityMpc
{
	String PROCESS_NAME_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE = "executeFeasibilityMpcMultiShare";
	String PROCESS_NAME_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE = "executeFeasibilityMpcSingleShare";
	String PROCESS_NAME_REQUEST_FEASIBILITY_MPC = "requestFeasibilityMpc";

	String PROCESS_NAME_FULL_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE = "highmedorg_"
			+ PROCESS_NAME_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE;
	String PROCESS_NAME_FULL_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE = "highmedorg_"
			+ PROCESS_NAME_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE;
	String PROCESS_NAME_FULL_REQUEST_FEASIBILITY_MPC = "highmedorg_" + PROCESS_NAME_REQUEST_FEASIBILITY_MPC;

	String BPMN_EXECUTION_VARIABLE_CORRELATION_KEY = "correlationKey";
	// String BPMN_EXECUTION_VARIABLE_CORRELATION_KEYS_SINGLE_MEDIC_SHARE = "correlationKeysSingleMedicShare";

	String BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_SINGLE_MEDIC_SHARES = "queryResultsSingleMedicShare";
	String BPMN_EXECUTION_VARIABLE_QUERY_RESULTS_MULTI_MEDIC_SHARES = "queryResultsMultiMedicShare";

	// Must be 3 or larger, as otherwise it is possible to draw conclusions about the individual MeDICs
	// (if I already know the cohort size in my MeDIC)
	int MIN_PARTICIPATING_MEDICS = 3;
	int MIN_COHORT_DEFINITIONS = 1;

	String FEASIBILITY_MPC_QUERY_PREFIX = "SELECT COUNT";

	String PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MPC = "http://highmed.org/fhir/StructureDefinition/task-request-feasibility-mpc";
	String PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MPC_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MPC
			+ "|" + VERSION;
	String PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MPC_PROCESS_URI = PROCESS_HIGHMED_URI_BASE
			+ PROCESS_NAME_REQUEST_FEASIBILITY_MPC + "/";
	String PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MPC_PROCESS_URI_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MPC_PROCESS_URI
			+ VERSION;
	String PROFILE_HIGHMED_TASK_REQUEST_FEASIBILITY_MPC_MESSAGE_NAME = "requestFeasibilityMpcMessage";

	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE = "http://highmed.org/fhir/StructureDefinition/task-execute-feasibility-mpc-multi-share";
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE
			+ "|" + VERSION;
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE_PROCESS_URI = PROCESS_HIGHMED_URI_BASE
			+ PROCESS_NAME_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE + "/";
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE_PROCESS_URI_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE_PROCESS_URI
			+ VERSION;
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_MULTI_SHARE_MESSAGE_NAME = "executeFeasibilityMpcMultiShareMessage";

	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE = "http://highmed.org/fhir/StructureDefinition/task-execute-feasibility-mpc-single-share";
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE
			+ "|" + VERSION;
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE_PROCESS_URI = PROCESS_HIGHMED_URI_BASE
			+ PROCESS_NAME_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE + "/";
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE_PROCESS_URI_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE_PROCESS_URI
			+ VERSION;
	String PROFILE_HIGHMED_TASK_EXECUTE_FEASIBILITY_MPC_SINGLE_SHARE_MESSAGE_NAME = "executeFeasibilityMpcSingleShareMessage";

	String PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_SHARE_FEASIBILITY_MPC = "http://highmed.org/fhir/StructureDefinition/task-single-medic-result-share-feasibility-mpc";
	String PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_SHARE_FEASIBILITY_MPC_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_SHARE_FEASIBILITY_MPC
			+ "|" + VERSION;
	String PROFILE_HIGHMED_TASK_SINGLE_MEDIC_RESULT_SHARE_FEASIBILITY_MPC_MESSAGE_NAME = "resultShareSingleMedicFeasibilityMpcMessage";

	String PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_SHARE_FEASIBILITY_MPC = "http://highmed.org/fhir/StructureDefinition/task-multi-medic-result-share-feasibility-mpc";
	String PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_SHARE_FEASIBILITY_MPC_AND_LATEST_VERSION = PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_SHARE_FEASIBILITY_MPC
			+ "|" + VERSION;
	String PROFILE_HIGHMED_TASK_MULTI_MEDIC_RESULT_SHARE_FEASIBILITY_MPC_MESSAGE_NAME = "resultShareMultiMedicFeasibilityMpcMessage";
}
