package org.batfish.coordinator.resources;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;

/**
 * Resource for servicing client API calls for network-wide or inferred node roles as applied to a
 * specific snapshot. It is a subresource of {@link SnapshotResource}.
 */
@ParametersAreNonnullByDefault
public final class SnapshotNodeRolesResource {

  private final boolean _inferred;
  private final String _network;
  private final String _snapshot;

  public SnapshotNodeRolesResource(String network, String snapshot, boolean inferred) {
    _network = network;
    _snapshot = snapshot;
    _inferred = inferred;
  }

  @Produces(MediaType.APPLICATION_JSON)
  @GET
  public Response getSnapshotNodeRoles() throws IOException {
    NodeRolesData data =
        _inferred
            ? Main.getWorkMgr().getSnapshotNodeRoles(_network, _snapshot)
            : Main.getWorkMgr().getNetworkNodeRoles(_network);
    if (data == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    Set<String> nodes = Main.getWorkMgr().getNodes(_network, _snapshot);
    if (nodes == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(new NodeRolesDataBean(data, _snapshot)).build();
  }

  @Path("/{dimension}")
  @Produces(MediaType.APPLICATION_JSON)
  @GET
  public Response getSnapshotNodeRolesDimension(@PathParam("dimension") String dimension)
      throws IOException {
    NodeRolesData data =
        _inferred
            ? Main.getWorkMgr().getSnapshotNodeRoles(_network, _snapshot)
            : Main.getWorkMgr().getNetworkNodeRoles(_network);
    Optional<NodeRoleDimension> nodeRolesDimension = data.nodeRoleDimensionFor(dimension);
    if (!nodeRolesDimension.isPresent()) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok()
        .entity(new NodeRoleDimensionBean(nodeRolesDimension.get(), _snapshot))
        .build();
  }
}
