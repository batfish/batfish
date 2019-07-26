package org.batfish.coordinator;

import static org.batfish.common.CoordConstsV2.QP_VERBOSE;

import com.google.common.base.Throwables;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.batfish.version.BatfishVersion;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

@Path(CoordConsts.SVC_CFG_POOL_MGR)
public class PoolMgrService {

  BatfishLogger _logger = Main.getLogger();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getInfo() {
    _logger.info("PMS:getInfo\n");
    return new JSONArray(
        Arrays.asList(
            CoordConsts.SVC_KEY_SUCCESS,
            "Batfish coordinator v"
                + BatfishVersion.getVersionStatic()
                + ". Enter ../application.wadl (relative to your URL) to see supported methods"));
  }

  @GET
  @Path(CoordConsts.SVC_RSC_POOL_GET_QUESTION_TEMPLATES)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getQuestionTemplates(@QueryParam(QP_VERBOSE) boolean verbose) {
    try {
      _logger.info("PMS:getQuestionTemplates\n");

      Map<String, String> questionTemplates = Main.getQuestionTemplates(verbose);

      if (questionTemplates == null) {
        return new JSONArray(
            Arrays.asList(CoordConsts.SVC_KEY_FAILURE, "Question templates dir is not configured"));
      } else {
        return new JSONArray(
            Arrays.asList(
                CoordConsts.SVC_KEY_SUCCESS,
                new JSONObject().put(CoordConsts.SVC_KEY_QUESTION_LIST, questionTemplates)));
      }
    } catch (Exception e) {
      _logger.errorf(
          "WMS:getQuestionTemplates exception: %s\n", Throwables.getStackTraceAsString(e));
      return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
    }
  }

  @GET
  @Path(CoordConsts.SVC_RSC_POOL_GETSTATUS)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getStatus() {
    try {
      _logger.info("PMS:getStatus\n");
      Map<String, String> poolStatus = Main.getPoolMgr().getPoolStatus();
      JSONObject obj = new JSONObject(poolStatus);
      return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, obj.toString()));
    } catch (Exception e) {
      _logger.errorf("PMS:getStatus exception: %s\n", Throwables.getStackTraceAsString(e));
      return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
    }
  }

  // functions for pool management
  @GET
  @Path(CoordConsts.SVC_RSC_POOL_UPDATE)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray updatePool(@Context UriInfo ui) {
    try {
      _logger.infof("PMS:updatePool got %s\n", ui);
      MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

      String workerVersion = null;
      List<String> workersToAdd = new LinkedList<>();
      List<String> workersToDelete = new LinkedList<>();

      for (MultivaluedMap.Entry<String, List<String>> entry : queryParams.entrySet()) {
        _logger.infof("PMS:updatePool: key = %s value = %s\n", entry.getKey(), entry.getValue());

        if (entry.getKey().equals(CoordConsts.SVC_KEY_ADD_WORKER)) {
          for (String worker : entry.getValue()) {
            // don't add empty values; occurs for keys without values
            if (!worker.equals("")) {
              workersToAdd.add(worker);
            }
          }
        } else if (entry.getKey().equals(CoordConsts.SVC_KEY_DEL_WORKER)) {
          for (String worker : entry.getValue()) {
            // don't add empty values; occurs for keys without values
            if (!worker.equals("")) {
              workersToDelete.add(worker);
            }
          }
        } else if (entry.getKey().equals(CoordConsts.SVC_KEY_VERSION)) {
          if (entry.getValue().size() > 1) {
            return new JSONArray(
                Arrays.asList(
                    CoordConsts.SVC_KEY_FAILURE,
                    "Got " + entry.getValue().size() + " version values"));
          }

          workerVersion = entry.getValue().get(0);
        } else {
          return new JSONArray(
              Arrays.asList(CoordConsts.SVC_KEY_FAILURE, "Got unknown command " + entry.getKey()));
        }
      }

      // we can delete without checking for version
      for (String worker : workersToDelete) {
        Main.getPoolMgr().deleteFromPool(worker);
      }

      if (!workersToAdd.isEmpty()) {
        if (workerVersion == null) {
          return new JSONArray(
              Arrays.asList(CoordConsts.SVC_KEY_FAILURE, "Worker version not specified"));
        }
        for (String worker : workersToAdd) {
          Main.getPoolMgr().addToPool(worker);
        }
      }
    } catch (Exception e) {
      _logger.errorf("PMS:updatePool exception: %s\n", Throwables.getStackTraceAsString(e));
      return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_FAILURE, e.getMessage()));
    }

    return new JSONArray(Arrays.asList(CoordConsts.SVC_KEY_SUCCESS, "done"));
  }
}
