package org.batfish.coordinator.resources;

import static org.batfish.common.CoordConstsV2.RSC_INPUT;
import static org.batfish.common.CoordConstsV2.RSC_OBJECTS;
import static org.batfish.common.CoordConstsV2.RSC_POJO_TOPOLOGY;
import static org.batfish.common.CoordConstsV2.RSC_TOPOLOGY;

import java.io.IOException;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;
import org.batfish.datamodel.TestrigMetadata;
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
  public Response get() throws IOException {
    org.batfish.datamodel.pojo.Topology topology =
        Main.getWorkMgr().getPojoTopology(_network, _snapshot);
    if (topology == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(topology).build();
  }

  @Path(RSC_INPUT)
  public SnapshotInputObjectsResource getSnapshotInputObjectsResource() {
    return new SnapshotInputObjectsResource(_network, _snapshot);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSnapshotMetadata() throws IOException {
    TestrigMetadata metadata = Main.getWorkMgr().getTestrigMetadata(_network, _snapshot);
    if (metadata == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(metadata).build();
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
}
