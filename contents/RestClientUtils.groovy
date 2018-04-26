import com.dtolabs.rundeck.core.Constants
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.nodes.ProjectNodeService
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import MesosFailReason
import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.log4j.Logger

/**
 * Created by carlos on 30/12/17.
 */
class RestClientUtils {
    public static final Logger logger = Logger.getLogger(RestClientUtils.class);

    public static final String URI_PATH = "/v2/apps/"
    public static final String URI_TASKS = '/tasks'

    public
    static putApp(String mesosServiceApiURL, String apiToken, String appId, Map properties, Map queryParams, PluginStepContext context) {
        logger.info("Calling put app on mesos service API...");

        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)

        if (apiToken) {
            Map headers = serviceAPI.getHeaders()
            headers["Authorization"] = "token=${apiToken}"
            headers['Accept'] = 'application/json'
            headers["Content-Type"] = "application/json; charset=utf8"
        }

        serviceAPI.request(Method.PUT, "application/json; charset=utf8") { req ->
            logger.info("Mounting requisition...")

            uri.path = uri.path + URI_PATH + appId
            uri.query = queryParams.findAll { it.value }
            body = JsonOutput.toJson(properties)

            response.success = { resp, json ->
                logger.info("Requisition returned status ${resp.status}")
                assert [200, 201].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                logger.info("Verifying if all tasks is running...")
                getTaskApp(mesosServiceApiURL, apiToken, appId, context,
                        context.getExecutionContext().loglevel == Constants.DEBUG_LEVEL, true)

                refreshNodeSet(context)
                logger.info("requisition finished with success. json response: ${json.toString()}")
                context.getExecutionContext().getExecutionListener().log(Constants.INFO_LEVEL, json.toString());
            }
            response.'400' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Invalid JSON")
                throw new StepException(
                        "Invalid JSON",
                        MesosFailReason.InvalidJSON
                );
            }
            response.'415' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Unsupported Media type error with JSON")
                throw new StepException(
                        "Unsupported Media type error with JSON",
                        MesosFailReason.InvalidJSON
                );
            }
            response.'405' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Not Allowed")
                throw new StepException(
                        "Not Allowed",
                        MesosFailReason.requestFailed
                );
            }
            response.'401' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Invalid User")
                throw new StepException(
                        "Invalid username or password.",
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Not Authorized")
                throw new StepException(
                        "Not Authorized to perform this action!",
                        MesosFailReason.NotAuthorized
                );
            }
            response.'409' = { resp ->
                logger.error("Requisition returned status ${resp.status} - App Already Exists")
                throw new StepException(
                        "An app with id [/existing_app] already exists.",
                        MesosFailReason.AppAlreadyExists
                );
            }
            response.'422' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Object is not valid")
                throw new StepException(
                        "Object is not valid",
                        MesosFailReason.InvalidObject
                );
            }
            response.failure = { resp, json ->
                logger.error("Requisition returned status ${resp.status} - Put app on mesos service error")
                throw new StepException(
                        "Put app on mesos service error",
                        MesosFailReason.requestFailed
                );
            }
        }
    }

    public static getTaskApp(String mesosServiceApiURL, String apiToken, String appId, PluginStepContext context, boolean showLog = true,
                      boolean verifyTasksRunning = false, int limitCalls = 10, int calls = 0) {
        logger.info("Calling get tasks of an app on mesos service API...")
        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)
        if (apiToken) {
            Map headers = serviceAPI.getHeaders()
            headers["Authorization"] = "token=${apiToken}"
            headers['Accept'] = 'application/json'
            headers["Content-Type"] = "application/json; charset=utf8"
        }

        serviceAPI.request(Method.GET, "application/json; charset=utf8") { req ->
            logger.info("Mounting requisition...")

            uri.path = uri.path + URI_PATH + appId + URI_TASKS
            uri.query = [:]
            response.success = { resp, json ->
                logger.info("Requisition returned status ${resp.status}")
                assert [200].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                List tasks = json.tasks
                if (verifyTasksRunning) {
                    logger.info("Verifying if tasks are running: Iterating on tasks...")
                    tasks.each { Map task ->
                        if (task.state != "TASK_RUNNING" && calls < limitCalls) {
                            logger.info("Task state: ${task.state}. Attempt: ${calls}")
                            calls++;
                            getTaskApp(mesosServiceApiURL, apiToken, appId, context,
                                    context.getExecutionContext().loglevel == Constants.DEBUG_LEVEL,
                                    true, limitCalls, calls)
                        }
                    }
                }
                refreshNodeSet(context)
                logger.info("requisition finished with success. json response: ${json.toString()}")
                if (showLog) context.getExecutionContext().getExecutionListener().log(Constants.INFO_LEVEL, json.toString())
            }
            response.'401' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Invalid username or password.")
                throw new StepException(
                        "Invalid username or password.",
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Not Authorized to perform this action!")
                throw new StepException(
                        "Not Authorized to perform this action!",
                        MesosFailReason.NotAuthorized
                );
            }
            response.'404' = { resp ->
                logger.error("Requisition returned status ${resp.status} - App does not exist")
                throw new StepException(
                        "App '/not_existent' does not exist",
                        MesosFailReason.AppNotExists
                );
            }
            response.failure = { resp, json ->
                logger.error("Requisition returned status ${resp.status} - Put app on mesos service error")
                throw new StepException(
                        "Put app on mesos service error",
                        MesosFailReason.requestFailed
                );
            }
        }
    }

    public static deleteTaskApp(String mesosServiceApiURL, String apiToken, String appId, Map queryParams, PluginStepContext context) {
        logger.info("Calling delete task of an app on mesos service API...")
        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)
        if (apiToken) {
            Map headers = serviceAPI.getHeaders()
            headers["Authorization"] = "token=${apiToken}"
            headers['Accept'] = 'application/json'
            headers["Content-Type"] = "application/json; charset=utf8"
        }

        serviceAPI.request(Method.DELETE, "application/json; charset=utf8") { req ->
            logger.info("Mounting requisition...")

            uri.path = uri.path + URI_PATH + appId + URI_TASKS
            uri.query = queryParams.findAll { it.value }

            response.success = { resp, json ->
                logger.info("Requisition returned status ${resp.status}")
                assert [200].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                logger.info("requisition finished with success. json response: ${json.toString()}")
                context.getExecutionContext().getExecutionListener().log(Constants.INFO_LEVEL, json.toString());
            }
            response.'401' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Invalid User")
                throw new StepException(
                        "Invalid username or password.",
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Not Authorized")
                throw new StepException(
                        "Not Authorized to perform this action!",
                        MesosFailReason.NotAuthorized
                );
            }
            response.'404' = { resp ->
                logger.error("Requisition returned status ${resp.status} - App '/not_existent' does not exist")
                throw new StepException(
                        "App '/not_existent' does not exist",
                        MesosFailReason.AppNotExists
                );
            }
            response.'409' = { resp ->
                logger.error("Requisition returned status ${resp.status} - App is locked by one or more deployments. Override with the option '?force=true'.")
                throw new StepException(
                        "App is locked by one or more deployments. Override with the option '?force=true'. View details at '/v2/deployments/<DEPLOYMENT_ID>'.",
                        MesosFailReason.AppAlreadyExists
                );
            }
            response.failure = { resp, json ->
                logger.error("Requisition returned status ${resp.status} - Put app on mesos service error")
                throw new StepException(
                        "Put app on mesos service error",
                        MesosFailReason.requestFailed
                );
            }
        }
    }

    public static deleteApp(String mesosServiceApiURL, String apiToken, String appId, Map queryParams, PluginStepContext context) {
        logger.info("Calling delete app on mesos service API...")
        HTTPBuilder serviceAPI = new HTTPBuilder(mesosServiceApiURL)
        if (apiToken) {
            Map headers = serviceAPI.getHeaders()
            headers["Authorization"] = "token=${apiToken}"
            headers['Accept'] = 'application/json'
            headers["Content-Type"] = "application/json; charset=utf8"
        }

        serviceAPI.request(Method.DELETE, "application/json; charset=utf8") { req ->
            logger.info("Mounting requisition...")

            uri.path = uri.path + URI_PATH + appId
            uri.query = queryParams.findAll { it.value }

            response.success = { resp, json ->
                logger.info("Requisition returned status ${resp.status}")
                assert [200].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");;

                refreshNodeSet(context)

                logger.info("requisition finished with success. json response: ${json.toString()}")
                context.getExecutionContext().getExecutionListener().event("log", json.toString(), meta);
            }
            response.'401' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Invalid User")
                throw new StepException(
                        "Invalid username or password.",
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                logger.error("Requisition returned status ${resp.status} - Not Authorized")
                throw new StepException(
                        "Not Authorized to perform this action!",
                        MesosFailReason.NotAuthorized
                );
            }
            response.'404' = { resp ->
                logger.error("Requisition returned status ${resp.status} - App '/not_existent' does not exist")
                throw new StepException(
                        "App '/not_existent' does not exist",
                        MesosFailReason.AppNotExists
                );
            }
            response.'409' = { resp ->
                logger.error("Requisition returned status ${resp.status} - App is locked by one or more deployments. Override with the option '?force=true'.")
                throw new StepException(
                        "App is locked by one or more deployments. Override with the option '?force=true'. View details at '/v2/deployments/<DEPLOYMENT_ID>'.",
                        MesosFailReason.AppAlreadyExists
                );
            }
            response.failure = { resp, json ->
                logger.error("Requisition returned status ${resp.status} - Put app on mesos service error")
                throw new StepException(
                        "Put app on mesos service error",
                        MesosFailReason.requestFailed
                );
            }
        }
    }

    private static void refreshNodeSet(PluginStepContext context) {
        logger.info("Refreshing node set...")
        ProjectNodeService nodeService = context.getExecutionContext().getNodeService();
        nodeService.refreshProjectNodes(context.getFrameworkProject())
        nodeService.getNodeSet(context.getFrameworkProject())
        logger.info("Node set refreshed...")
    }
}
