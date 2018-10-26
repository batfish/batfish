package org.batfish.coordinator.resources;

import static org.batfish.common.util.HttpUtil.checkClientArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
 * The {@link NetworkNodeRolesResource} is a resource for servicing client API calls for
 * network-wide node roles. It is a subresource of {@link NetworkResource}.
 *
 * <p>This resource provides information about the roles of a container using GET.
 */
@Produces(MediaType.APPLICATION_JSON)
public final class NetworkNodeRolesResource {

  @VisibleForTesting
  static boolean noDuplicateDimensions(NodeRolesDataBean nodeRolesDataBean) {
    if (nodeRolesDataBean.roleDimensions == null) {
      return false;
    }
    int uniqueSize =
        nodeRolesDataBean
            .roleDimensions
            .stream()
            .map(bean -> bean.name)
            .map(String::toLowerCase)
            .collect(ImmutableSet.toImmutableSet())
            .size();
    return uniqueSize == nodeRolesDataBean.roleDimensions.size();
  }

  private String _network;

  public NetworkNodeRolesResource(String network) {
    _network = network;
  }

  @POST
  public Response addNodeRoleDimension(NodeRoleDimensionBean dimBean) throws IOException {
    checkClientArgument(dimBean.name != null, "Node role dimension must have a name");
    NodeRolesData nodeRolesData = Main.getWorkMgr().getNetworkNodeRoles(_network);
    if (nodeRolesData == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    Optional<NodeRoleDimension> dimension = nodeRolesData.getNodeRoleDimension(dimBean.name);
    checkClientArgument(!dimension.isPresent(), "Duplicate dimension specified: %s", dimBean.name);
    if (!Main.getWorkMgr()
        .putNetworkNodeRoles(
            NodeRolesData.builder()
                .setDefaultDimension(nodeRolesData.getDefaultDimension())
                .setRoleDimensions(
                    ImmutableSortedSet.<NodeRoleDimension>naturalOrder()
                        .addAll(nodeRolesData.getNodeRoleDimensions())
                        .add(dimBean.toNodeRoleDimension())
                        .build())
                .build(),
            _network)) {
      // if network was deleted while we were working
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  /** Relocate the request to {@link NetworkNodeRoleDimensionResource}. */
  @Path("/{dimension}")
  public NetworkNodeRoleDimensionResource getNodeRoleDimensionResource(
      @PathParam("dimension") String dimension) {
    return new NetworkNodeRoleDimensionResource(_network, dimension);
  }

  /** Returns information about node roles in the container */
  @GET
  public Response getNodeRoles() throws IOException {
    NodeRolesData nodeRolesData = Main.getWorkMgr().getNetworkNodeRoles(_network);
    if (nodeRolesData == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok()
        .entity(new NodeRolesDataBean(nodeRolesData, null, ImmutableSet.of()))
        .build();
  }

  @Consumes(MediaType.APPLICATION_JSON)
  @PUT
  public Response putNodeRoles(NodeRolesDataBean nodeRolesDataBean) throws IOException {
    checkClientArgument(nodeRolesDataBean != null, "Node roles must not be null");
    checkClientArgument(
        noDuplicateDimensions(nodeRolesDataBean),
        "Supplied node roles contains duplicate dimensions");
    if (!Main.getWorkMgr().putNetworkNodeRoles(nodeRolesDataBean.toNodeRolesData(), _network)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }
}
