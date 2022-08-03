package org.batfish.coordinator;

import com.google.common.base.Throwables;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Task;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.config.Settings;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.uri.UriComponent;

/** Legacy pool-based assigner implementation. */
@ParametersAreNonnullByDefault
public class PoolAssigner implements Assigner {

  /** A task handler that checks work by querying the worker assigned from the pool. */
  private class PoolTaskHandle implements TaskHandle {

    public PoolTaskHandle(QueuedWork work, String worker) {
      _work = work;
      _worker = worker;
    }

    private final @Nonnull QueuedWork _work;
    private final @Nonnull String _worker;

    @Override
    public Task checkTask() {
      _logger.debugf("WM:CheckWork: Trying to check %s on %s\n", _work, _worker);
      Task task = new Task(TaskStatus.UnreachableOrBadResponse);
      Client client = null;
      try {
        client = CommonUtil.createHttpClientBuilder().build();

        String protocol = _settings.getSslPoolDisable() ? "http" : "https";
        WebTarget webTarget =
            client
                .target(
                    String.format(
                        "%s://%s%s/%s",
                        protocol, _worker, BfConsts.SVC_BASE_RSC, BfConsts.SVC_GET_TASKSTATUS_RSC))
                .queryParam(
                    BfConsts.SVC_TASKID_KEY,
                    UriComponent.encode(
                        _work.getId().toString(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));

        JSONArray array;
        try (Response response = webTarget.request(MediaType.APPLICATION_JSON).get()) {
          if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _logger.errorf("WM:CheckTask: Got non-OK response %s\n", response.getStatus());
            return task;
          }
          String sobj = response.readEntity(String.class);
          array = new JSONArray(sobj);
        }
        _logger.debugf("WM:CheckTask: response: %s [%s] [%s]\n", array, array.get(0), array.get(1));

        if (!array.get(0).equals(BfConsts.SVC_SUCCESS_KEY)) {
          _logger.error(
              String.format(
                  "got error while refreshing status: %s %s\n", array.get(0), array.get(1)));
        } else {
          String taskStr = array.get(1).toString();
          task = BatfishObjectMapper.mapper().readValue(taskStr, Task.class);
          if (task.getStatus() == null) {
            _logger.error("did not see status key in json response\n");
          }
        }
      } catch (ProcessingException e) {
        String stackTrace = Throwables.getStackTraceAsString(e);
        _logger.error(String.format("unable to connect to %s: %s\n", _worker, stackTrace));
      } catch (Exception e) {
        String stackTrace = Throwables.getStackTraceAsString(e);
        _logger.error(String.format("exception: %s\n", stackTrace));
      } finally {
        if (client != null) {
          client.close();
        }
      }

      if (_work.getStatus() == WorkStatusCode.TERMINATEDBYUSER) {
        return new Task(TaskStatus.TerminatedByUser);
      }

      return task;
    }

    @Override
    public void postTermination() {
      Main.getPoolMgr().refreshWorkerStatus(_worker);
    }
  }

  public PoolAssigner(BatfishLogger logger, Settings settings) {
    _logger = logger;
    _settings = settings;
  }

  @Override
  public AssignmentResult assign(QueuedWork work) {
    String idleWorker = Main.getPoolMgr().getWorkerForAssignment();

    // get out if no idle worker was found, but release the work first
    if (idleWorker == null) {
      _logger.info("WM:AssignWork: No idle worker\n");
      return new FailureAssignmentResult(work);
    }
    return assignWork(work, idleWorker);
  }

  private @Nonnull AssignmentResult assignWork(QueuedWork work, String worker) {

    _logger.infof("WM:AssignWork: Trying to assign %s to %s\n", work, worker);

    boolean assignmentError = false;
    boolean assigned = false;

    Client client = null;
    try {
      // get the task and add other standard stuff
      JSONObject task = new JSONObject(work.resolveRequestParams());
      task.put(
          BfConsts.ARG_STORAGE_BASE,
          Main.getSettings().getContainersLocation().toAbsolutePath().toString());

      client = CommonUtil.createHttpClientBuilder().build();

      String protocol = _settings.getSslPoolDisable() ? "http" : "https";
      WebTarget webTarget =
          client
              .target(
                  String.format(
                      "%s://%s%s/%s",
                      protocol, worker, BfConsts.SVC_BASE_RSC, BfConsts.SVC_RUN_TASK_RSC))
              .queryParam(
                  BfConsts.SVC_TASKID_KEY,
                  UriComponent.encode(
                      work.getId().toString(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
              .queryParam(
                  BfConsts.SVC_TASK_KEY,
                  UriComponent.encode(
                      task.toString(), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED));

      JSONArray array;
      try (Response response = webTarget.request(MediaType.APPLICATION_JSON).get()) {
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
          _logger.errorf("WM:AssignWork: Got non-OK response %s\n", response.getStatus());
          // previous to refactoring, returned with no value
          return new FailureAssignmentResult(work);
        }
        String sobj = response.readEntity(String.class);
        array = new JSONArray(sobj);
      }
      _logger.info(
          String.format(
              "WM:AssignWork: response: %s [%s] [%s]\n",
              array.toString(), array.get(0), array.get(1)));

      if (!array.get(0).equals(BfConsts.SVC_SUCCESS_KEY)) {
        _logger.error(
            String.format("ERROR in assigning task: %s %s\n", array.get(0), array.get(1)));

        assignmentError = true;
      } else {
        assigned = true;
      }
    } catch (ProcessingException e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.error(String.format("Unable to connect to worker at %s: %s\n", worker, stackTrace));
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.error(String.format("Exception assigning work: %s\n", stackTrace));
    } finally {
      if (client != null) {
        client.close();
      }
    }

    if (work.getStatus() == WorkStatusCode.TERMINATEDBYUSER) {
      return TerminatedAssignmentResult.instance();
    }

    // mark the assignment results for both work and worker
    if (assignmentError) {
      return new ErrorAssignmentResult(work);
    } else if (assigned) {
      try {
        Main.getPoolMgr().markAssignmentResult(worker, assigned);
      } catch (Exception e) {
        String stackTrace = Throwables.getStackTraceAsString(e);
        _logger.errorf("Unable to markAssignment for work %s: %s\n", work, stackTrace);
        return new FailureAssignmentResult(work);
      }
      return new SuccessAssignmentResult(work, new PoolTaskHandle(work, worker));
    } else {
      return new FailureAssignmentResult(work);
    }
  }

  private final @Nonnull BatfishLogger _logger;
  private final @Nonnull Settings _settings;
}
