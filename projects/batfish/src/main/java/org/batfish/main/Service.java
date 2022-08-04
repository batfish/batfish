package org.batfish.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.LaunchResult;
import org.batfish.common.Task;
import org.batfish.common.util.BatfishObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

@Path(BfConsts.SVC_BASE_RSC)
public class Service {

  BatfishLogger _logger = Driver.getMainLogger();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getInfo() {
    return new JSONArray(
        Arrays.asList(
            BfConsts.SVC_SUCCESS_KEY,
            "Batfish service: enter ../application.wadl (relative to your URL) to see supported "
                + "methods"));
  }

  @GET
  @Path(BfConsts.SVC_GET_STATUS_RSC)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getStatus() {
    try {
      return new JSONArray(
          Arrays.asList(
              BfConsts.SVC_SUCCESS_KEY,
              (new JSONObject().put("idle", Driver.getIdle())).toString()));
    } catch (Exception e) {
      return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, e.getMessage()));
    }
  }

  @GET
  @Path(BfConsts.SVC_GET_TASKSTATUS_RSC)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getTaskStatus(@QueryParam(BfConsts.SVC_TASKID_KEY) String taskId) {
    _logger.debugf("BFS:getTaskStatus %s\n", taskId);
    try {

      if (taskId == null || taskId.equals("")) {
        return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, "taskid not supplied"));
      }

      Task task = Driver.getBatfishWorkerService().getTaskStatus(taskId);
      String taskStr = BatfishObjectMapper.writeString(task);
      return new JSONArray(Arrays.asList(BfConsts.SVC_SUCCESS_KEY, taskStr));
    } catch (Exception e) {
      return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, e.getMessage()));
    }
  }

  @GET
  @Path(BfConsts.SVC_RUN_TASK_RSC)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray runTask(
      @QueryParam(BfConsts.SVC_TASKID_KEY) String taskId,
      @QueryParam(BfConsts.SVC_TASK_KEY) String task) {
    _logger.infof("BFS:runTask(%s,%s)\n", taskId, task);
    try {

      if (taskId == null || taskId.equals("")) {
        return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, "taskid not supplied"));
      }

      if (task == null || task.equals("")) {
        return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, "task not supplied"));
      }

      List<String> argsList = new ArrayList<>();

      JSONObject taskObj = new JSONObject(task);
      Iterator<?> keys = taskObj.keys();

      while (keys.hasNext()) {

        String key = (String) keys.next();
        String value = taskObj.getString(key);

        argsList.add("-" + key);

        if (value != null && !value.equals("")) {
          argsList.add(value);
        }
      }

      String[] args = argsList.toArray(new String[argsList.size()]);

      _logger.infof("Will run with args: %s\n", Arrays.toString(args));
      LaunchResult lr = Driver.getBatfishWorkerService().runTask(taskId, args);

      switch (lr.getType()) {
        case BUSY:
          return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, "worker not idle"));
        case ERROR:
          return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, lr.getMessage()));
        case LAUNCHED:
          return new JSONArray(
              Arrays.asList(BfConsts.SVC_SUCCESS_KEY, String.format("launched %s", taskId)));
        default:
          throw new IllegalArgumentException(
              String.format("Invalid LaunchResult.Type: %s", lr.getType()));
      }
    } catch (Exception e) {
      return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, e.getMessage()));
    }
  }
}
