package org.batfish.coordinator.resources;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;

/**
 * The {@link NetworkNodeRoleDimensionResource} is a resource for servicing client API calls for
 * node role dimensions. It is a subresource of {@link NetworkNodeRolesResource}.
 *
 * <p>This resource provides information about the role dimension using GET. It also allows
 * modifications and creating of new dimensions using PUT.
 */
@Produces(MediaType.APPLICATION_JSON)
@ParametersAreNonnullByDefault
public final class NetworkNodeRoleDimensionResource {

  private final String _network;
  private final String _dimension;

  public NetworkNodeRoleDimensionResource(String network, String dimension) {
    _network = network;
    _dimension = dimension;
  }

  @DELETE
  public Response delNodeRoleDimension() throws IOException {
    NodeRolesData nodeRolesData = Main.getWorkMgr().getNetworkNodeRoles(_network);
    if (nodeRolesData == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    Optional<NodeRoleDimension> dimension = nodeRolesData.getNodeRoleDimension(_dimension);
    if (!dimension.isPresent()) {
      return Response.status(Status.NOT_FOUND).build();
    }
    nodeRolesData
        .getNodeRoleDimensions()
        .removeIf(dim -> dim.getName().equalsIgnoreCase(dimension.get().getName()));
    // if network was deleted while we were working
    if (!Main.getWorkMgr().putNetworkNodeRoles(nodeRolesData, _network)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  @GET
  public Response getNodeRoleDimension() throws IOException {
    NodeRolesData nodeRolesData = Main.getWorkMgr().getNetworkNodeRoles(_network);
    if (nodeRolesData == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    Optional<NodeRoleDimension> dimension = nodeRolesData.getNodeRoleDimension(_dimension);
    if (!dimension.isPresent()) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok()
        .entity(new NodeRoleDimensionBean(dimension.get(), null, ImmutableSet.of()))
        .build();
  }
}
