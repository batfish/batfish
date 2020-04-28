package org.batfish.coordinator.resources;

import static org.batfish.common.CoordConstsV2.RSC_COMPLETED_WORK;
import static org.batfish.common.CoordConstsV2.RSC_INFERRED_NODE_ROLES;
import static org.batfish.common.CoordConstsV2.RSC_INPUT;
import static org.batfish.common.CoordConstsV2.RSC_NODE_ROLES;
import static org.batfish.common.CoordConstsV2.RSC_OBJECTS;
import static org.batfish.common.CoordConstsV2.RSC_POJO_TOPOLOGY;
import static org.batfish.common.CoordConstsV2.RSC_TOPOLOGY;
import static org.batfish.common.CoordConstsV2.RSC_WORK_JSON;
import static org.batfish.common.CoordConstsV2.RSC_WORK_LOG;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.Topology;

/** Resource for servicing client API calls for a specific snapshot */
@ParametersAreNonnullByDefault
public final class SnapshotResource {

  private final String _network;

  private final String _snapshot;

  public SnapshotResource(String network, String snapshot) {
    _network = network;
    _snapshot = snapshot;
  }

  @Path(RSC_POJO_TOPOLOGY)
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  public Response getPojoTopology() throws IOException {
    org.batfish.datamodel.pojo.Topology topology =
        Main.getWorkMgr().getPojoTopology(_network, _snapshot);
    if (topology == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(topology).build();
  }

  @Path(RSC_INFERRED_NODE_ROLES)
  public SnapshotNodeRolesResource getSnapshotInferredNodeRoles() {
    return new SnapshotNodeRolesResource(_network, _snapshot, true);
  }

  @Path(RSC_INPUT)
  public SnapshotInputObjectsResource getSnapshotInputObjectsResource() {
    return new SnapshotInputObjectsResource(_network, _snapshot);
  }

  @DELETE
  public Response deleteSnapshot() {
    if (!Main.getWorkMgr().delSnapshot(_network, _snapshot)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  /**
   * Get completed work for the specified network's snapshot.
   *
   * @return List of {@link WorkBean}
   */
  @Path(RSC_COMPLETED_WORK)
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  public Response getCompletedWork() {
    try {
      List<WorkBean> completedWork =
          Main.getWorkMgr().getCompletedWork(_network, _snapshot).stream()
              .map(WorkBean::new)
              .collect(Collectors.toList());
      return Response.ok().entity(completedWork).build();
    } catch (IllegalArgumentException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSnapshotMetadata() throws IOException {
    SnapshotMetadata metadata = Main.getWorkMgr().getSnapshotMetadata(_network, _snapshot);
    if (metadata == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(metadata).build();
  }

  @Path(RSC_NODE_ROLES)
  public SnapshotNodeRolesResource getSnapshotNodeRoles() {
    return new SnapshotNodeRolesResource(_network, _snapshot, false);
  }

  @Path(RSC_OBJECTS)
  public SnapshotObjectsResource getSnapshotObjectsResource() {
    return new SnapshotObjectsResource(_network, _snapshot);
  }

  @Path(RSC_TOPOLOGY)
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  public Response getTopology() throws IOException {
    Topology topology = Main.getWorkMgr().getTopology(_network, _snapshot);
    if (topology == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(topology).build();
  }

  @Path(RSC_WORK_LOG + "/{workid}")
  @Produces(MediaType.TEXT_PLAIN)
  @GET
  public Response getWorkLog(@PathParam("workid") String workId) throws IOException {
    String log = Main.getWorkMgr().getWorkLog(_network, _snapshot, workId);
    if (log == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.status(Status.OK).entity(log).build();
  }

  @Path(RSC_WORK_JSON + "/{workid}")
  @Produces(MediaType.TEXT_PLAIN)
  @GET
  public Response getWorkJson(@PathParam("workid") String workId) throws IOException {
    String json = Main.getWorkMgr().getWorkJson(_network, _snapshot, workId);
    if (json == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.status(Status.OK).entity(json).build();
  }
}
