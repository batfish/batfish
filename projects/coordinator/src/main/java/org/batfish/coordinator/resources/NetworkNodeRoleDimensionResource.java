package org.batfish.coordinator.resources;

import static org.batfish.common.util.HttpUtil.checkClientArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;
import org.batfish.role.RoleMapping;

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
    Optional<NodeRoleDimension> dimension = nodeRolesData.nodeRoleDimensionFor(_dimension);
    if (!dimension.isPresent()) {
      return Response.status(Status.NOT_FOUND).build();
    }
    if (!Main.getWorkMgr()
        .putNetworkNodeRoles(delDimensionFromNodeRolesData(_dimension, nodeRolesData), _network)) {
      // if network was deleted while we were working
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  private NodeRolesData delDimensionFromNodeRolesData(
      String dimension, NodeRolesData nodeRolesData) {
    return NodeRolesData.builder()
        .setDefaultDimension(nodeRolesData.getDefaultDimension())
        .setType(nodeRolesData.getType())
        .setRoleMappings(
            nodeRolesData.getRoleMappings().stream()
                .map(m -> delDimensionFromRoleMapping(dimension, m))
                .filter(Objects::nonNull)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  private RoleMapping delDimensionFromRoleMapping(String dimension, RoleMapping m) {
    RoleMapping result =
        new RoleMapping(
            m.getName().orElse(null),
            m.getRegex(),
            delDimensionFromMap(dimension, m.getRoleDimensionsGroups()),
            delDimensionFromMap(dimension, m.getCanonicalRoleNames()));
    if (result.getRoleDimensionsGroups().isEmpty()) {
      // this role mapping is useless so let's delete it entirely
      return null;
    } else {
      return result;
    }
  }

  private <V> Map<String, V> delDimensionFromMap(String dimension, Map<String, V> map) {
    return map.entrySet().stream()
        .filter(e -> !e.getKey().equalsIgnoreCase(dimension))
        .collect(
            ImmutableMap.toImmutableMap(
                Map.Entry<String, V>::getKey, Map.Entry<String, V>::getValue));
  }

  @GET
  public Response getNodeRoleDimension() throws IOException {
    NodeRolesData nodeRolesData = Main.getWorkMgr().getNetworkNodeRoles(_network);
    if (nodeRolesData == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    Optional<NodeRoleDimension> dimension = nodeRolesData.nodeRoleDimensionFor(_dimension);
    if (!dimension.isPresent()) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(new NodeRoleDimensionBean(dimension.get(), null)).build();
  }

  /**
   * Adds the supplied node role dimension. If one of the same name already exists, it is
   * overwritten.
   */
  @PUT
  public Response putNodeRoleDimension(NodeRoleDimensionBean dimBean) throws IOException {
    checkClientArgument(dimBean.name != null, "Node role dimension must have a name");
    NodeRolesData nodeRolesData = Main.getWorkMgr().getNetworkNodeRoles(_network);
    if (nodeRolesData == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    // delete any old version of this dimension
    NodeRolesData cleanedNRD = delDimensionFromNodeRolesData(dimBean.name, nodeRolesData);
    if (!Main.getWorkMgr()
        .putNetworkNodeRoles(
            NodeRolesData.builder()
                .setDefaultDimension(cleanedNRD.getDefaultDimension())
                .setType(cleanedNRD.getType())
                .setRoleMappings(
                    ImmutableList.<RoleMapping>builder()
                        .addAll(cleanedNRD.getRoleMappings())
                        .addAll(dimBean.toNodeRoleDimension().toRoleMappings())
                        .build())
                .build(),
            _network)) {
      // if network was deleted while we were working
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }
}
