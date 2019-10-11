package org.batfish.coordinator.resources;

import static org.batfish.common.util.HttpUtil.checkClientArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.batfish.role.RoleMapping;

/**
 * The {@link NetworkNodeRolesResource} is a resource for servicing client API calls for
 * network-wide node roles. It is a subresource of {@link NetworkResource}.
 *
 * <p>This resource provides information about the roles of a container using GET.
 */
@Produces(MediaType.APPLICATION_JSON)
public final class NetworkNodeRolesResource {

  @VisibleForTesting
  static boolean noDuplicateRoleMappings(NodeRolesDataBean nodeRolesDataBean) {
    if (nodeRolesDataBean.roleMappings == null) {
      return true;
    }
    List<String> names =
        nodeRolesDataBean.roleMappings.stream()
            .map(bean -> bean.name)
            .filter(Objects::nonNull)
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    return names.size() == ImmutableSet.copyOf(names).size();
  }

  private String _network;

  public NetworkNodeRolesResource(String network) {
    _network = network;
  }

  /**
   * Deprecated in favor of {@link
   * NetworkNodeRoleDimensionResource#putNodeRoleDimension(NodeRoleDimensionBean)}
   */
  @Deprecated
  @POST
  public Response addNodeRoleDimension(NodeRoleDimensionBean dimBean) throws IOException {
    checkClientArgument(dimBean.name != null, "Node role dimension must have a name");
    NodeRolesData nodeRolesData = Main.getWorkMgr().getNetworkNodeRoles(_network);
    if (nodeRolesData == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    Optional<NodeRoleDimension> dimension = nodeRolesData.nodeRoleDimensionFor(dimBean.name);
    checkClientArgument(!dimension.isPresent(), "Duplicate dimension specified: %s", dimBean.name);
    if (!Main.getWorkMgr()
        .putNetworkNodeRoles(
            NodeRolesData.builder()
                .setDefaultDimension(nodeRolesData.getDefaultDimension())
                .setRoleMappings(
                    ImmutableList.<RoleMapping>builder()
                        .addAll(nodeRolesData.getRoleMappings())
                        .addAll(dimBean.toNodeRoleDimension().toRoleMappings())
                        .build())
                .setRoleDimensionOrder(nodeRolesData.getRoleDimensionOrder().orElse(null))
                .setType(nodeRolesData.getType())
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
    return Response.ok().entity(new NodeRolesDataBean(nodeRolesData, null)).build();
  }

  @Consumes(MediaType.APPLICATION_JSON)
  @PUT
  public Response putNodeRoles(NodeRolesDataBean nodeRolesDataBean) throws IOException {
    checkClientArgument(nodeRolesDataBean != null, "Node roles must not be null");
    checkClientArgument(
        noDuplicateRoleMappings(nodeRolesDataBean),
        "Supplied node roles contains duplicate role mappings");
    if (!Main.getWorkMgr().putNetworkNodeRoles(nodeRolesDataBean.toNodeRolesData(), _network)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }
}
