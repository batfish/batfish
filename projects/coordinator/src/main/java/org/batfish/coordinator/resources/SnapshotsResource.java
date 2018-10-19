package org.batfish.coordinator.resources;

import static org.batfish.common.CoordConstsV2.QP_VERBOSE;

import java.io.IOException;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;

/** Resource for servicing snapshot-related client API calls at snapshot-global level */
@ParametersAreNonnullByDefault
@Produces(MediaType.APPLICATION_JSON)
public final class SnapshotsResource {

  private final String _network;

  public SnapshotsResource(String network) {
    _network = network;
  }

  @Path("/{snapshot}")
  public SnapshotResource getSnapshotResource(@PathParam("snapshot") String snapshot) {
    return new SnapshotResource(_network, snapshot);
  }

  @GET
  public Response listSnapshots(@QueryParam(QP_VERBOSE) boolean verbose) throws IOException {
    List<?> result =
        verbose
            ? Main.getWorkMgr().listSnapshotsWithMetadata(_network)
            : Main.getWorkMgr().listSnapshots(_network);
    if (result == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(result).build();
  }
}
