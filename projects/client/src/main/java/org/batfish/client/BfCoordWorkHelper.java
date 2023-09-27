package org.batfish.client;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.client.config.Settings;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.CoordConstsV2;
import org.batfish.common.WorkItem;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.pojo.WorkStatus;

public class BfCoordWorkHelper {

  private final String _coordWorkMgrV2;
  private final BatfishLogger _logger;
  private final Settings _settings;
  private final Client _client;

  public BfCoordWorkHelper(BatfishLogger logger, Settings settings) {
    _logger = logger;
    _settings = settings;
    _coordWorkMgrV2 = _settings.getCoordinatorHost() + ":" + _settings.getCoordinatorWorkV2Port();
    try {
      _client = getClientBuilder().build();
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      throw new BatfishException("Failed to create HTTP client", e);
    }
  }

  public boolean delNetwork(String networkName) {
    WebTarget webTarget = getTargetV2(Lists.newArrayList(CoordConstsV2.RSC_NETWORKS, networkName));

    try (Response response =
        webTarget
            .request(MediaType.APPLICATION_JSON)
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
            .delete()) {

      if (response.getStatusInfo().getFamily() != Status.Family.SUCCESSFUL) {
        _logger.errorf("delNetwork: Did not get OK response. Got: %s\n", response.getStatus());
        _logger.error(response.readEntity(String.class) + "\n");
        return false;
      }
      return true;
    } catch (Exception e) {
      _logger.errorf("Exception in delNetwork from %s for %s\n", _coordWorkMgrV2, networkName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return false;
    }
  }

  private ClientBuilder getClientBuilder() {
    return CommonUtil.createHttpClientBuilder();
  }

  /**
   * Returns the JSON log of a work item completed by the batfish worker corresponding to a given
   * work ID.
   */
  public @Nullable String getWorkJson(String networkName, String snapshotName, UUID workId) {
    WebTarget webTarget =
        getTargetV2(
            Arrays.asList(
                CoordConstsV2.RSC_NETWORKS,
                networkName,
                CoordConstsV2.RSC_SNAPSHOTS,
                snapshotName,
                CoordConstsV2.RSC_WORK_JSON,
                workId.toString()));
    try (Response response =
        webTarget
            .request(MediaType.TEXT_PLAIN)
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
            .get()) {
      _logger.debug(response.getStatus() + " " + response.getStatusInfo() + " " + response + "\n");
      if (response.getStatusInfo().getFamily() != Status.Family.SUCCESSFUL) {
        _logger.debugf(
            "getWorkJson: Did not get an OK response for %s -> %s->%s\n",
            networkName, snapshotName, workId);
        return null;
      }
      return response.readEntity(String.class);
    } catch (Exception e) {
      _logger.errorf(
          "Exception in getWorkJson from %s using (%s, %s)\n",
          _coordWorkMgrV2, snapshotName, workId);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  /** Returns the JSON-encoded POJO topology for a snapshot. */
  public @Nullable String getPojoTopology(String networkName, String snapshotName) {
    WebTarget webTarget =
        getTargetV2(
            Arrays.asList(
                CoordConstsV2.RSC_NETWORKS,
                networkName,
                CoordConstsV2.RSC_SNAPSHOTS,
                snapshotName,
                CoordConstsV2.RSC_POJO_TOPOLOGY));
    try (Response response =
        webTarget
            .request(MediaType.APPLICATION_JSON)
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
            .get()) {
      _logger.debug(response.getStatus() + " " + response.getStatusInfo() + " " + response + "\n");
      if (response.getStatusInfo().getFamily() != Status.Family.SUCCESSFUL) {
        _logger.debugf(
            "getPojoTopology: Did not get an OK response for %s -> %s\n",
            networkName, snapshotName);
        return null;
      }
      return response.readEntity(String.class);
    } catch (Exception e) {
      _logger.errorf(
          "Exception in getPojoTopology from %s using %s\n", _coordWorkMgrV2, snapshotName);
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  /**
   * Returns a {@link WebTarget webTarget} for BatFish service V2 by resolving each segment in
   * {@code resources} to base url of service V2.
   */
  public WebTarget getTargetV2(List<String> resources) {
    String urlString = String.format("http://%s%s", _coordWorkMgrV2, CoordConsts.SVC_CFG_WORK_MGR2);
    WebTarget target = _client.target(urlString);
    for (String resource : resources) {
      target = target.path(resource);
    }
    return target;
  }

  public static class WorkResult {
    private final @Nonnull WorkStatusCode _status;
    private final @Nonnull String _taskStr;

    WorkResult(@Nonnull WorkStatusCode status, @Nonnull String taskStr) {
      _status = status;
      _taskStr = taskStr;
    }

    public @Nonnull WorkStatusCode getStatus() {
      return _status;
    }

    public @Nonnull String getTaskStr() {
      return _taskStr;
    }
  }

  @Nullable
  WorkResult getWorkStatus(UUID workId, String network) {
    WebTarget webTarget =
        getTargetV2(
            ImmutableList.of(
                CoordConstsV2.RSC_NETWORKS, network, CoordConstsV2.RSC_WORK, workId.toString()));
    try (Response response =
        webTarget
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
            .get()) {
      if (response.getStatus() != Status.OK.getStatusCode()) {
        _logger.errorf("getWorkStatus: Did not get OK response. Got: %s\n", response.getStatus());
        _logger.error(response.readEntity(String.class) + "\n");
        return null;
      }
      WorkStatus workStatus = response.readEntity(WorkStatus.class);
      return new WorkResult(
          workStatus.getWorkStatusCode(),
          BatfishObjectMapper.writeString(workStatus.getTaskStatusStr()));
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  public @Nullable String initNetwork(
      @Nullable String networkName, @Nullable String networkPrefix) {
    WebTarget webTarget = getTargetV2(ImmutableList.of(CoordConstsV2.RSC_NETWORKS));
    if (networkName != null) {
      webTarget = webTarget.queryParam(CoordConstsV2.QP_NAME, networkName);
    }
    if (networkPrefix != null) {
      webTarget = webTarget.queryParam(CoordConstsV2.QP_NAME_PREFIX, networkPrefix);
    }
    try (Response response =
        webTarget
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
            .post(Entity.entity(null, MediaType.APPLICATION_JSON))) {

      if (response.getStatus() != Status.CREATED.getStatusCode()) {
        _logger.errorf(
            "initNetwork: Did not get CREATED response. Got: %s\n", response.getStatus());
        _logger.error(response.readEntity(String.class) + "\n");
        return null;
      }
      String[] components = response.getLocation().getPath().split("/", -1);
      String encodedNetworkName = components[components.length - 1];
      return URLDecoder.decode(encodedNetworkName, StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return null;
    }
  }

  public boolean isReachable(boolean chatty) {

    WebTarget webTarget = getTargetV2(ImmutableList.of(CoordConstsV2.RSC_VERSION));

    try (Response response =
        webTarget
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
            .get()) {
      _logger.info(response.getStatus() + " " + response.getStatusInfo() + " " + response + "\n");

      if (response.getStatusInfo().getFamily() != Status.Family.SUCCESSFUL) {
        _logger.errorf("isReachable: Did not get an OK response\n");
        return false;
      }

      return true;
    } catch (ProcessingException e) {
      if (e.getMessage().contains("ConnectException")) {
        if (chatty) {
          _logger.errorf("BF-Client: unable to connect to coordinator at %s\n", webTarget.getUri());
        }
        return false;
      }
      if (e.getMessage().contains("SSLHandshakeException")) {
        if (chatty) {
          _logger.errorf(
              "SSL handshake exception while connecting to coordinator (Is the coordinator using "
                  + "SSL and using keys that you trust?): %s\n",
              e.getMessage());
        }
        return false;
      }
      if (e.getMessage().contains("SocketException: Unexpected end of file")) {
        if (chatty) {
          _logger.errorf("SocketException while connecting to coordinator. (Are you using SSL?)\n");
        }
        return false;
      }
      throw e;
    }
  }

  public boolean queueWork(WorkItem wItem) {
    WebTarget webTarget =
        getTargetV2(
            ImmutableList.of(
                CoordConstsV2.RSC_NETWORKS, wItem.getNetwork(), CoordConstsV2.RSC_WORK));

    try (Response response =
        webTarget
            .request()
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
            .post(Entity.entity(wItem, MediaType.APPLICATION_JSON))) {
      if (response.getStatus() != Status.CREATED.getStatusCode()) {
        _logger.errorf("queueWork: Did not get CREATED response. Got: %s\n", response.getStatus());
        _logger.error(response.readEntity(String.class) + "\n");
        return false;
      }
      return true;
    } catch (Exception e) {
      _logger.errorf("exception: ");
      _logger.error(Throwables.getStackTraceAsString(e) + "\n");
      return false;
    }
  }

  public boolean uploadQuestion(String networkName, String qName, String qFileName) {
    WebTarget webTarget =
        getTargetV2(
            ImmutableList.of(
                CoordConstsV2.RSC_NETWORKS, networkName, CoordConstsV2.RSC_QUESTIONS, qName));
    try (InputStream content = Files.newInputStream(Paths.get(qFileName))) {
      try (Response response =
          webTarget
              .request()
              .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
              .put(Entity.entity(content, MediaType.TEXT_PLAIN))) {
        if (response.getStatus() != Status.OK.getStatusCode()) {
          _logger.errorf(
              "uploadQuestion: Did not get OK response. Got: %s\n", response.getStatus());
          _logger.error(response.readEntity(String.class) + "\n");
          return false;
        }
        return true;
      }
    } catch (Exception e) {
      if (e.getMessage().contains("FileNotFoundException")) {
        _logger.errorf("File not found: %s (question file)\n", qFileName);
      } else {
        _logger.errorf(
            "Exception when uploading question to %s using (%s, %s, %s): %s\n",
            _coordWorkMgrV2, networkName, qName, qFileName, Throwables.getStackTraceAsString(e));
      }
      return false;
    }
  }

  public boolean uploadSnapshot(String networkName, String snapshotName, String zipfileName) {
    WebTarget webTarget =
        getTargetV2(
            ImmutableList.of(
                CoordConstsV2.RSC_NETWORKS,
                networkName,
                CoordConstsV2.RSC_SNAPSHOTS,
                snapshotName));

    try (InputStream zipInput = Files.newInputStream(Paths.get(zipfileName))) {
      try (Response response =
          webTarget
              .request()
              .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey())
              .post(Entity.entity(zipInput, MediaType.APPLICATION_OCTET_STREAM))) {

        if (response.getStatus() != Status.CREATED.getStatusCode()) {
          _logger.errorf(
              "uploadSnapshot: Did not get CREATED response. Got: %s\n", response.getStatus());
          _logger.error(response.readEntity(String.class) + "\n");
          return false;
        }
        return true;
      } catch (Exception e) {
        _logger.errorf(
            "Exception in uploadSnapshot from %s for network %s, snapshot %s\n",
            _coordWorkMgrV2, networkName, snapshotName);
        _logger.error(Throwables.getStackTraceAsString(e) + "\n");
        return false;
      }
    } catch (Exception e) {
      if (e.getMessage().contains("FileNotFoundException")) {
        _logger.errorf("File not found: %s\n", zipfileName);
      } else {
        _logger.errorf(
            "Exception when uploading snapshot to %s using (%s, %s, %s): %s\n",
            _coordWorkMgrV2,
            networkName,
            snapshotName,
            zipfileName,
            Throwables.getStackTraceAsString(e));
      }
      return false;
    }
  }

  public boolean debugV2(
      FileWriter outWriter, String method, String urlTail, Object entity, MediaType mediaType)
      throws IOException {
    WebTarget webTarget = getTargetV2(Arrays.asList(urlTail.split("/", -1)));
    Invocation.Builder builder =
        webTarget
            .request(MediaType.APPLICATION_JSON)
            .header(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY, _settings.getApiKey());
    try (Response response =
        entity != null
            ? builder.method(method, Entity.entity(entity, mediaType))
            : builder.method(method)) {
      if (response.hasEntity()) {
        String output = response.readEntity(String.class);
        if (outWriter != null) {
          outWriter.write(output);
        } else {
          _logger.outputf("%s\n", output);
        }
      }
      return response.getStatus() == Status.OK.getStatusCode();
    }
  }
}
