package org.batfish.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.batfish.common.BatfishConstants;
import org.batfish.common.BatfishConstants.WorkStatus;

@Path(BatfishConstants.SERVICE_BASE_RESOURCE)
public class Service {

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getInfo() {
      return new JSONArray(
            Arrays.asList(
                  "",
                  "Batfish service: enter ../application.wadl (relative to your URL) to see supported methods"));
   }

   @GET
   @Path(BatfishConstants.SERVICE_GETSTATUS_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getStatus() {
      try {
         return new JSONArray(Arrays.asList("",
               (new JSONObject().put("idle", Driver.getIdle())).toString()));
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }
   }

   @GET
   @Path(BatfishConstants.SERVICE_GETWORKSTATUS_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray getWorkStatus(@QueryParam(BatfishConstants.SERVICE_WORKID_KEY) String workId) {
      try {
         
         if (workId == null || workId.equals("")) {
            return new JSONArray(Arrays.asList("failure", "workid not supplied"));            
         }

         Work work = Driver.getWorkFromLog(workId);

         if (work == null) {
            return new JSONArray(Arrays.asList("",
                  (new JSONObject().put("status", WorkStatus.Unknown.toString()).toString())));
         }
         
         return new JSONArray(Arrays.asList("",
               (new JSONObject().put("status", Driver.getWorkFromLog(workId).getStatus().toString()).toString())));
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }
   }

   @GET
   @Path(BatfishConstants.SERVICE_RUN_RESOURCE)
   @Produces(MediaType.APPLICATION_JSON)
   public JSONArray run(@Context UriInfo ui) {
      try {
         MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

         List<String> argsList = new ArrayList<String>();

         String workId = null;
         
         for (MultivaluedMap.Entry<String, List<String>> entry : queryParams
               .entrySet()) {
            System.out.printf("key = %s value = %s\n", entry.getKey(),
                  entry.getValue());

            //pull out the special key corresponding to workid
            if (entry.getKey().equals(BatfishConstants.SERVICE_WORKID_KEY)) {
               workId = entry.getValue().get(0);
            }
            else {
               argsList.add("-" + entry.getKey());

               for (String value : entry.getValue()) {
                  // don't add empty values; occurs for options that have no
                  // value
                  if (!value.equals("")) {
                     argsList.add(value);
                  }
               }
            }
         }

         if (workId == null) {
            return new JSONArray(Arrays.asList("failure",
                  "WorkId was not supplied"));
         }
         
         String[] args = argsList.toArray(new String[argsList.size()]);

         System.out.printf("Will run with args: %s\n", Arrays.toString(args));

         return new JSONArray(Driver.RunBatfishThroughService(workId, args));
      }
      catch (Exception e) {
         return new JSONArray(Arrays.asList("failure", e.getMessage()));
      }
   }
}
