package org.batfish.coordinator.resources;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.batfish.common.CoordConstsV2.RSC_FORK;
import static org.batfish.common.CoordConstsV2.RSC_NODE_ROLES;
import static org.batfish.common.CoordConstsV2.RSC_OBJECTS;
import static org.batfish.common.CoordConstsV2.RSC_REFERENCE_LIBRARY;
import static org.batfish.common.CoordConstsV2.RSC_SETTINGS;
import static org.batfish.common.CoordConstsV2.RSC_SNAPSHOTS;

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
 * The {@link ContainerResource} is a resource for servicing client API calls at container level.
 *
 * <p>The ContainerResource provides information about a specified container, and provides the
 * ability to delete a specified container for authenticated clients.
 *
 * <p>The ContainerResource also provides the access entry for other subResources.
 */
@Produces(MediaType.APPLICATION_JSON)
public class ContainerResource {

  private BatfishLogger _logger = Main.getLogger();

  private String _name;

  public ContainerResource(String apiKey, String name) {
    checkAccessToContainer(apiKey, name);
    _name = name;
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

  /** Relocate the request to {@link ReferenceLibraryResource}. */
  @Path(RSC_REFERENCE_LIBRARY)
  public ReferenceLibraryResource getReferenceLibraryResource() {
    return new ReferenceLibraryResource(_name);
  }

  /** Relocate the request to {@link NetworkSettingsResource}. */
  @Path(RSC_SETTINGS)
  public NetworkSettingsResource getSettingsResource() {
    return new NetworkSettingsResource(_name);
  }

  /** Delete a specified container with name: {@link #_name}. */
  @DELETE
  public Response deleteContainer() {
    _logger.infof("WMS2: delNetwork '%s'\n", _name);
    if (Main.getWorkMgr().delNetwork(_name)) {
      return Response.noContent().build();
    } else {
      return Response.serverError().build();
    }
  }

  /** Check if {@code container} exists and {@code apiKey} has access to it. */
  private static void checkAccessToContainer(String apiKey, String container) {
    if (!Main.getWorkMgr().checkContainerExists(container)) {
      throw new NotFoundException(String.format("Network '%s' does not exist", container));
    }

    if (!Main.getAuthorizer().isAccessibleContainer(apiKey, container, false)) {
      throw new ForbiddenException(
          String.format("network '%s' is not accessible by the api key: %s", container, apiKey));
    }
  }

  /** Relocate the request to {@link SnapshotsResource} */
  @Path(RSC_SNAPSHOTS)
  public SnapshotsResource getSnapshotsResource() {
    return new SnapshotsResource(_name);
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
