package org.batfish.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.batfish.common.BfConsts;
import org.batfish.common.WorkItem;
import org.batfish.common.BfConsts.TaskStatus;

@Path(BfConsts.SVC_BASE_RSC)
public class Service {

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      return new JSONArray(
            Arrays.asList(
                  BfConsts.SVC_SUCCESS_KEY,
                  "Batfish service: enter ../application.wadl (relative to your URL) to see supported methods"));
   }

   @GET
   @Path(BfConsts.SVC_GET_STATUS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getStatus() {
      try {
         return new JSONArray(Arrays.asList(BfConsts.SVC_SUCCESS_KEY,
               (new JSONObject().put("idle", Driver.getIdle())).toString()));
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
   }

   @GET
   @Path(BfConsts.SVC_GET_TASKSTATUS_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getTaskStatus(@QueryParam(BfConsts.SVC_TASKID_KEY) String taskId) {
      try {
         
         if (taskId == null || taskId.equals("")) {
            return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, "taskid not supplied"));            
         }

         Task task = Driver.getTaskkFromLog(taskId);

         if (task == null) {
            return new JSONArray(Arrays.asList(BfConsts.SVC_SUCCESS_KEY,
                  (new JSONObject().put("status", TaskStatus.Unknown.toString()).toString())));
         }
         
         return new JSONArray(Arrays.asList(BfConsts.SVC_SUCCESS_KEY,
               (new JSONObject().put("status", Driver.getTaskkFromLog(taskId).getStatus().toString()).toString())));
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
   }

   @GET
   @Path(BfConsts.SVC_RUN_TASK_RSC)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray runTask(
         @QueryParam(BfConsts.SVC_TASKID_KEY) String taskId,
         @QueryParam(BfConsts.SVC_TASK_KEY) String task) {
      try {

         if (taskId == null || taskId.equals("")) {
            return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, "taskid not supplied"));            
         }

         if (task == null || task.equals("")) {
            return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, "task not supplied"));            
         }

         List<String> argsList = new ArrayList<String>();
         
         WorkItem workItem = new WorkItem(task);
         
         for (Entry<String, String> entry : workItem.getRequestParams()
               .entrySet()) {

            argsList.add("-" + entry.getKey());

            if (entry.getValue() != null && !entry.getValue().equals("")) {
               argsList.add(entry.getValue());
            }
         }
         
         String[] args = argsList.toArray(new String[argsList.size()]);

         System.out.printf("Will run with args: %s\n", Arrays.toString(args));

         return new JSONArray(Driver.RunBatfishThroughService(taskId, args));
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList(BfConsts.SVC_FAILURE_KEY, e.getMessage()));
      }
   }
}
