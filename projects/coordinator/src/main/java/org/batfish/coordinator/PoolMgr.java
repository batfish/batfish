package org.batfish.coordinator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.config.Settings;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class PoolMgr {

  static final class WorkerStatusRefreshTask implements Runnable {
    @Override
    public void run() {
      Main.getPoolMgr().refreshWorkerStatus();
    }
  }

  private final BatfishLogger _logger;

  private final Settings _settings;

  // the key should be of the form <ip or hostname>:<port>
  private Map<String, WorkerStatus> _workerPool;

  public PoolMgr(Settings settings, BatfishLogger logger) {
    _settings = settings;
    _logger = logger;
    _workerPool = new HashMap<>();
  }

  public synchronized void addToPool(final String worker) {
    // start out as unknown and trigger refresh in the background
    _workerPool.put(worker, new WorkerStatus(WorkerStatus.StatusCode.UNKNOWN));

    Thread thread =
        new Thread() {
          @Override
          public void run() {
            refreshWorkerStatus(worker);
          }
        };

    thread.start();
  }

  public synchronized void deleteFromPool(String worker) {
    if (_workerPool.containsKey(worker)) {
      _workerPool.remove(worker);
    }
  }

  private synchronized List<String> getAllWorkers() {
    List<String> workers = new LinkedList<>();
    workers.addAll(_workerPool.keySet());
    return workers;
  }

  public synchronized int getNumWorkers() {
    return _workerPool.size();
  }

  public synchronized HashMap<String, String> getPoolStatus() {
    HashMap<String, String> copy = new HashMap<>();

    for (Entry<String, WorkerStatus> entry : _workerPool.entrySet()) {
      copy.put(entry.getKey(), entry.getValue().toString());
    }

    return copy;
  }

  @Nullable
  public synchronized String getWorkerForAssignment() {

    for (Entry<String, WorkerStatus> workerEntry : _workerPool.entrySet()) {
      if (workerEntry.getValue().getStatus() == WorkerStatus.StatusCode.IDLE) {
        updateWorkerStatus(workerEntry.getKey(), WorkerStatus.StatusCode.TRYINGTOASSIGN);
        return workerEntry.getKey();
      }
    }

    return null;
  }

  @Nullable
  public WorkerStatus getWorkerStatus(String worker) {
    return _workerPool.get(worker);
  }

  public void markAssignmentResult(String worker, boolean assignmentSuccessful) {
    updateWorkerStatus(
        worker, assignmentSuccessful ? WorkerStatus.StatusCode.BUSY : WorkerStatus.StatusCode.IDLE);
  }

  public void refreshWorkerStatus() {
    // _logger.info("PM:RefreshWorkerStatus: entered\n");
    List<String> workers = getAllWorkers();
    for (String worker : workers) {
      refreshWorkerStatus(worker);
    }
  }

  public void refreshWorkerStatus(String worker) {
    // _logger.debug("PM:RefreshWorkerStatus: refreshing status of " + worker
    // +"\n");
    Client client = null;
    try {
      // Client client = ClientBuilder.newClient();
      client =
          CommonUtil.createHttpClientBuilder(
                  _settings.getSslPoolDisable(),
                  _settings.getSslPoolTrustAllCerts(),
                  _settings.getSslPoolKeystoreFile(),
                  _settings.getSslPoolKeystorePassword(),
                  _settings.getSslPoolTruststoreFile(),
                  _settings.getSslPoolTruststorePassword(),
                  false)
              .build();
      String protocol = _settings.getSslPoolDisable() ? "http" : "https";
      WebTarget webTarget =
          client.target(
              String.format(
                  "%s://%s%s/%s",
                  protocol, worker, BfConsts.SVC_BASE_RSC, BfConsts.SVC_GET_STATUS_RSC));
      Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
      Response response = invocationBuilder.get();

      // _logger.debug(webTarget.getUri());

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        _logger.errorf("PM:RefreshWorkerStatus: Got non-OK response %s\n", response.getStatus());
      } else {
        String sobj = response.readEntity(String.class);
        JSONArray array = new JSONArray(sobj);

        // _logger.info(String.format("response: %s [%s] [%s]\n",
        // array.toString(), array.get(0), array.get(1)));

        if (!array.get(0).equals(BfConsts.SVC_SUCCESS_KEY)) {
          _logger.error(
              String.format(
                  "got error while refreshing status: %s %s\n", array.get(0), array.get(1)));
          updateWorkerStatus(worker, WorkerStatus.StatusCode.UNKNOWN);
          return;
        }

        JSONObject jObj = new JSONObject(array.get(1).toString());

        if (!jObj.has("idle")) {
          _logger.error(String.format("did not see idle key in json response\n"));
          updateWorkerStatus(worker, WorkerStatus.StatusCode.UNKNOWN);
          return;
        }

        boolean status = jObj.getBoolean("idle");

        // update the status, except leave the ones with TRYINGTOASSIGN
        // alone
        if (getWorkerStatus(worker).getStatus() != WorkerStatus.StatusCode.TRYINGTOASSIGN) {
          updateWorkerStatus(
              worker, status ? WorkerStatus.StatusCode.IDLE : WorkerStatus.StatusCode.BUSY);
        }
      }
    } catch (ProcessingException e) {
      _logger.error(String.format("unable to connect to %s: %s\n", worker, e.getMessage()));
      updateWorkerStatus(worker, WorkerStatus.StatusCode.UNREACHABLE);
    } catch (Exception e) {
      String stackTrace = ExceptionUtils.getStackTrace(e);
      _logger.error(String.format("exception: %s\n", stackTrace));
      updateWorkerStatus(worker, WorkerStatus.StatusCode.UNKNOWN);
    } finally {
      if (client != null) {
        client.close();
      }
    }
  }

  public void startPoolManager() {
    Runnable workerStatusRefreshTask = new WorkerStatusRefreshTask();
    Executors.newScheduledThreadPool(1)
        .scheduleWithFixedDelay(
            workerStatusRefreshTask,
            0,
            Main.getSettings().getPeriodWorkerStatusRefreshMs(),
            TimeUnit.MILLISECONDS);
  }

  private synchronized void updateWorkerStatus(String worker, WorkerStatus.StatusCode statusCode) {
    if (_workerPool.containsKey(worker)) {
      _workerPool.get(worker).updateStatus(statusCode);
    }
  }
}
