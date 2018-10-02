package org.batfish.coordinator;

import static org.batfish.common.CoordConstsV2.KEY_RESULT;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.opentracing.util.GlobalTracer;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.resources.ContainerResource;
import org.batfish.coordinator.resources.ForkSnapshotBean;
import org.codehaus.jettison.json.JSONObject;

/**
 * The Work Manager is a RESTful service for servicing client API calls.
 *
 * <p>The Work Manager provides information about the containers, testrigs, questions, and analyses
 * in the Batfish service based on the credentials of the client, and provides authenticated clients
 * the ability to create/delete or otherwise modify the same.
 */
@Path(CoordConsts.SVC_CFG_WORK_MGR2)
@Produces(MediaType.APPLICATION_JSON)
public class WorkMgrServiceV2 {

  private BatfishLogger _logger = Main.getLogger();

  @DefaultValue(CoordConsts.DEFAULT_API_KEY)
  @HeaderParam(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY)
  private String _apiKey;

  /** Information on the URI of a request, injected by the server framework at runtime. */
  @Context private UriInfo _uriInfo;

  private static void checkStringParam(String paramStr, String parameterName) {
    if (Strings.isNullOrEmpty(paramStr)) {
      throw new IllegalArgumentException(parameterName + " is missing or empty");
    }
  }

  /**
   * Fork the specified snapshot and make changes to the new snapshot
   *
   * @param apiKey The API key of the client
   * @param clientVersion The version of the client
   * @param networkName The name of the network under which to fork the snapshot
   * @param snapshotName The name of the new snapshot to create
   * @param forkSnapshotBean The {@link ForkSnapshotBean} containing parameters used to create the
   *     fork
   * @return TODO: document JSON response
   */
  @POST
  @Path(CoordConstsV2.RSC_NETWORKS + "/{network}/" + CoordConstsV2.RSC_SNAPSHOTS + "/{snapshot}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response forkSnapshot(
      @PathParam("network") String networkName,
      @PathParam("snapshot") String snapshotName,
      @HeaderParam(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY) String apiKey,
      @HeaderParam(CoordConstsV2.HTTP_HEADER_BATFISH_VERSION) String clientVersion,
      ForkSnapshotBean forkSnapshotBean) {
    try {
      _logger.infof("WMS2:forkSnapshot %s %s %s\n", apiKey, networkName, snapshotName);

      checkStringParam(apiKey, "API key");
      checkStringParam(clientVersion, "Client version");
      checkStringParam(networkName, "Network name");
      checkStringParam(snapshotName, "Snapshot name");

      // TODO determine if we need these as well
      // checkApiKeyValidity(apiKey);
      // checkClientVersion(clientVersion);
      // checkNetworkAccessibility(apiKey, networkName);

      if (GlobalTracer.get().activeSpan() != null) {
        GlobalTracer.get()
            .activeSpan()
            .setTag("network-name", networkName)
            .setTag("snapshot-name", snapshotName);
      }

      Main.getWorkMgr().forkSnapshot(apiKey, networkName, snapshotName, forkSnapshotBean);
      _logger.infof(
          "Created snapshot:%s forked from snapshot: %s for network:%s using api-key:%s\n",
          snapshotName, forkSnapshotBean.baseSnapshot, networkName, apiKey);
      return Response.ok(new JSONObject().put(KEY_RESULT, "Successfully forked snapshot")).build();
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      _logger.errorf(
          "WMS2:forkSnapshot exception for apikey:%s in network:%s, snapshot:%s; exception:%s",
          apiKey, networkName, snapshotName, stackTrace);
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
  }

  /**
   * Returns the list of {@link Container containers} that the given API key may access. Deprecated
   * in favor of {@link #getNetworks()} getNetworks}.
   */
  @GET
  @Path(CoordConstsV2.RSC_CONTAINERS)
  @Deprecated
  public Response getContainers() {
    return getNetworks();
  }

  /** Returns the list of {@link Container networks} that the given API key may access. */
  @GET
  @Path(CoordConstsV2.RSC_NETWORKS)
  public Response getNetworks() {
    _logger.info("WMS2:getNetworks\n");
    List<Container> containers = Main.getWorkMgr().getContainers(_apiKey);
    return Response.ok(containers).build();
  }

  /**
   * Redirect to /networks if the user does not supply a network ID. Deprecated in favor of {@link
   * #redirectNetwork() redirectNetwork}.
   */
  @GET
  @Path(CoordConstsV2.RSC_CONTAINER)
  @Deprecated
  public Response redirectContainer() {
    return redirectNetwork();
  }

  /** Redirect to /networks if the user does not supply a network ID. */
  @GET
  @Path(CoordConstsV2.RSC_NETWORK)
  public Response redirectNetwork() {
    _logger.info("WMS2:redirect network\n");
    return Response.status(Status.MOVED_PERMANENTLY)
        .location(_uriInfo.getRequestUri().resolve(CoordConstsV2.RSC_NETWORKS))
        .build();
  }

  /**
   * Relocate the request to ContainerResource. Deprecated in favor of {@link
   * #getNetworkResource(String)} getNetworkResource}.
   */
  @Path(CoordConstsV2.RSC_CONTAINERS + "/{id}")
  @Deprecated
  public ContainerResource getContainerResource(@PathParam("id") String id) {
    return getNetworkResource(id);
  }

  /** Relocate the request to ContainerResource. */
  @Path(CoordConstsV2.RSC_NETWORKS + "/{id}")
  public ContainerResource getNetworkResource(@PathParam("id") String id) {
    return new ContainerResource(_apiKey, id);
  }
}
