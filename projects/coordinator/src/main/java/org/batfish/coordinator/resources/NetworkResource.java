package org.batfish.coordinator.resources;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.batfish.common.CoordConstsV2.RSC_ANALYSES;
import static org.batfish.common.CoordConstsV2.RSC_FORK;
import static org.batfish.common.CoordConstsV2.RSC_NODE_ROLES;
import static org.batfish.common.CoordConstsV2.RSC_OBJECTS;
import static org.batfish.common.CoordConstsV2.RSC_QUESTIONS;
import static org.batfish.common.CoordConstsV2.RSC_REFERENCE_LIBRARY;
import static org.batfish.common.CoordConstsV2.RSC_SNAPSHOTS;
import static org.batfish.common.CoordConstsV2.RSC_WORK;

import io.opentracing.util.GlobalTracer;
import java.io.FileNotFoundException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.coordinator.Main;

/**
 * The {@link NetworkResource} is a resource for servicing client API calls at network level.
 *
 * <p>The NetworkResource provides information about a specified network, and provides the ability
 * to delete a specified network for authenticated clients.
 *
 * <p>The NetworkResource also provides the access entry for other subResources.
 */
@Produces(MediaType.APPLICATION_JSON)
public class NetworkResource {

  private BatfishLogger _logger = Main.getLogger();

  private String _name;

  public NetworkResource(String apiKey, String name) {
    checkAccessToNetwork(apiKey, name);
    _name = name;
  }

  /** Relocate the request to {@link AnalysesResource} */
  @Path(RSC_ANALYSES)
  public AnalysesResource getAnalysesResource() {
    return new AnalysesResource(_name);
  }

  /** Returns information about the given {@link Container}, provided this user can access it. */
  @GET
  public Response getContainer() {
    _logger.infof("WMS2: getNetwork '%s'\n", _name);
    Container container = Main.getWorkMgr().getContainer(_name);
    return Response.ok(container).build();
  }

  /** Relocate the request to {@link NetworkObjectsResource} */
  @Path(RSC_OBJECTS)
  public NetworkObjectsResource getNetworkObjectsResource() {
    return new NetworkObjectsResource(_name);
  }

  /** Relocate the request to {@link NetworkNodeRolesResource}. */
  @Path(RSC_NODE_ROLES)
  public NetworkNodeRolesResource getNodeRolesResource() {
    return new NetworkNodeRolesResource(_name);
  }

  /** Relocate the request to {@link QuestionsResource}. */
  @Path(RSC_QUESTIONS)
  public QuestionsResource getAdHocQuestionsResource() {
    return new QuestionsResource(_name, null);
  }

  /** Relocate the request to {@link ReferenceLibraryResource}. */
  @Path(RSC_REFERENCE_LIBRARY)
  public ReferenceLibraryResource getReferenceLibraryResource() {
    return new ReferenceLibraryResource(_name);
  }

  /** Delete a specified network with name: {@link #_name}. */
  @DELETE
  public Response deleteNetwork() {
    if (!Main.getWorkMgr().delNetwork(_name)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  /** Check if {@code network} exists and {@code apiKey} has access to it. */
  private static void checkAccessToNetwork(String apiKey, String network) {
    if (!Main.getWorkMgr().checkNetworkExists(network)) {
      throw new NotFoundException(String.format("Network '%s' does not exist", network));
    }

    if (!Main.getAuthorizer().isAccessibleNetwork(apiKey, network, false)) {
      throw new ForbiddenException(
          String.format("network '%s' is not accessible by the api key: %s", network, apiKey));
    }
  }

  /** Relocate the request to {@link SnapshotsResource} */
  @Path(RSC_SNAPSHOTS)
  public SnapshotsResource getSnapshotsResource() {
    return new SnapshotsResource(_name);
  }

  /** Relocate the request to {@link WorkResource} */
  @Path(RSC_WORK)
  public WorkResource getWorkResource() {
    return new WorkResource();
  }

  /**
   * Fork the specified snapshot and make changes to the new snapshot
   *
   * @param forkSnapshotBean The {@link ForkSnapshotBean} containing parameters used to create the
   *     fork
   */
  @POST
  @Path(RSC_SNAPSHOTS + ":" + RSC_FORK)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response forkSnapshot(ForkSnapshotBean forkSnapshotBean) {
    try {
      checkArgument(
          !isNullOrEmpty(forkSnapshotBean.newSnapshot), "Parameter %s is required", "new snapshot");
      checkArgument(
          !isNullOrEmpty(forkSnapshotBean.baseSnapshot),
          "Parameter %s is required",
          "base snapshot");

      // Set the appropriate tags for the trace being captured
      if (GlobalTracer.get().activeSpan() != null) {
        GlobalTracer.get()
            .activeSpan()
            .setTag("network-name", _name)
            .setTag("snapshot-name", forkSnapshotBean.newSnapshot);
      }

      Main.getWorkMgr().forkSnapshot(_name, forkSnapshotBean);
      return Response.ok().build();
    } catch (FileNotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
    } catch (IllegalArgumentException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }
}
