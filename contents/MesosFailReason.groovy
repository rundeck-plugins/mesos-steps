import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason

public enum MesosFailReason implements FailureReason {
    InvalidJSON,
    InvalidUser,
    NotAuthorized,
    AppAlreadyExists,
    InvalidObject,
    requestFailed,
    AppNotExists
}