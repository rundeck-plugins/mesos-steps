import com.dtolabs.rundeck.core.Constants
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.core.nodes.ProjectNodeService
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import MesosFailReason
import groovy.json.JsonOutput
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

/**
 * Created by carlos on 30/12/17.
 */
class RestClientUtils {
    public static final String URI_PATH = "/v2/apps/"
    public static final String URI_TASKS = '/tasks'

    public static putApp(String mesosServiceApiURL, String appId, Map properties, Map queryParams, PluginStepContext context){
        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)
        serviceAPI.request(Method.PUT, ContentType.JSON){ req ->
            uri.path = URI_PATH + appId
            uri.query = queryParams.findAll {it.value}
            body = JsonOutput.toJson(properties)

            response.success = { resp, json ->
                assert [200, 201].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                getTaskApp(mesosServiceApiURL, appId, context,
                        context.getExecutionContext().loglevel == Constants.DEBUG_LEVEL, true)

                refreshNodeSet(context)

                context.getExecutionContext().getExecutionListener().log(Constants.INFO_LEVEL, json.toString());
            }
            response.'400' = { resp ->
                throw new StepException(
                        "Invalid JSON",
                        MesosFailReason.InvalidJSON
                );
            }
            response.'401' = { resp ->
                throw new StepException(
                        "Invalid username or password.",
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                throw new StepException(
                        "Not Authorized to perform this action!",
                        MesosFailReason.NotAuthorized
                );
            }
            response.'409' = { resp ->
                throw new StepException(
                        "An app with id [/existing_app] already exists.",
                        MesosFailReason.AppAlreadyExists
                );
            }
            response.'422' = { resp ->
                throw new StepException(
                        "Object is not valid",
                        MesosFailReason.InvalidObject
                );
            }
            response.failure = { resp, json ->
                throw new StepException(
                        "Put app on mesos service error",
                        MesosFailReason.requestFailed
                );
            }
        }
    }

    public static getTaskApp(String mesosServiceApiURL, String appId, PluginStepContext context, boolean showLog = true,
                             boolean verifyTasksRunning = false, int limitCalls = 10, int calls = 0){
        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)
        serviceAPI.request(Method.GET, ContentType.JSON){ req ->
            uri.path = URI_PATH + appId + URI_TASKS
            uri.query = [:]

            response.success = { resp, json ->
                assert [200].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                List tasks = json.tasks
                if(verifyTasksRunning){
                    tasks.each {Map task ->
                        if(task.state != "TASK_RUNNING" && calls < limitCalls){
                            calls++;
                            getTaskApp(mesosServiceApiURL, appId, context,
                                    context.getExecutionContext().loglevel == Constants.DEBUG_LEVEL,
                                    true, limitCalls, calls)
                        }
                    }
                }
                refreshNodeSet(context)
                if(showLog) context.getExecutionContext().getExecutionListener().log(Constants.INFO_LEVEL, json.toString())
            }
            response.'401' = { resp ->
                throw new StepException(
                        "Invalid username or password.",
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                throw new StepException(
                        "Not Authorized to perform this action!",
                        MesosFailReason.NotAuthorized
                );
            }
            response.'404' = { resp ->
                throw new StepException(
                        "App '/not_existent' does not exist",
                        MesosFailReason.AppNotExists
                );
            }
            response.failure = { resp, json ->
                throw new StepException(
                        "Put app on mesos service error",
                        MesosFailReason.requestFailed
                );
            }
        }
    }

    public static deleteTaskApp(String mesosServiceApiURL, String appId, Map queryParams, PluginStepContext context){
        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)
        serviceAPI.request(Method.DELETE, ContentType.JSON){ req ->
            uri.path = URI_PATH + appId + URI_TASKS
            uri.query = queryParams.findAll {it.value}

            response.success = { resp, json ->
                assert [200].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                context.getExecutionContext().getExecutionListener().log(Constants.INFO_LEVEL, json.toString());
            }
            response.'401' = { resp ->
                throw new StepException(
                        "Invalid username or password.",
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                throw new StepException(
                        "Not Authorized to perform this action!",
                        MesosFailReason.NotAuthorized
                );
            }
            response.'404' = { resp ->
                throw new StepException(
                        "App '/not_existent' does not exist",
                        MesosFailReason.AppNotExists
                );
            }
            response.'409' = { resp ->
                throw new StepException(
                        "App is locked by one or more deployments. Override with the option '?force=true'. View details at '/v2/deployments/<DEPLOYMENT_ID>'.",
                        MesosFailReason.AppAlreadyExists
                );
            }
            response.failure = { resp, json ->
                throw new StepException(
                        "Put app on mesos service error",
                        MesosFailReason.requestFailed
                );
            }
        }
    }

    public static deleteApp(String mesosServiceApiURL, String appId, Map queryParams, PluginStepContext context){
        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)
        serviceAPI.request(Method.DELETE, ContentType.JSON){ req ->
            uri.path = URI_PATH + appId
            uri.query = queryParams.findAll {it.value}

            response.success = { resp, json ->
                assert [200].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                context.getExecutionContext().getExecutionListener().event("log", json.toString(), meta);
            }
            response.'401' = { resp ->
                throw new StepException(
                        "Invalid username or password.",
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                throw new StepException(
                        "Not Authorized to perform this action!",
                        MesosFailReason.NotAuthorized
                );
            }
            response.'404' = { resp ->
                throw new StepException(
                        "App '/not_existent' does not exist",
                        MesosFailReason.AppNotExists
                );
            }
            response.'409' = { resp ->
                throw new StepException(
                        "App is locked by one or more deployments. Override with the option '?force=true'. View details at '/v2/deployments/<DEPLOYMENT_ID>'.",
                        MesosFailReason.AppAlreadyExists
                );
            }
            response.failure = { resp, json ->
                throw new StepException(
                        "Put app on mesos service error",
                        MesosFailReason.requestFailed
                );
            }
        }
    }

    private static void refreshNodeSet(PluginStepContext context){
        ProjectNodeService nodeService = context.getExecutionContext().getNodeService();
        nodeService.refreshProjectNodes(context.getFrameworkProject())
        nodeService.getNodeSet(context.getFrameworkProject())
    }
}
