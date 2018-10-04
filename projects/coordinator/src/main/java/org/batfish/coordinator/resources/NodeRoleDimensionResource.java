package org.batfish.coordinator.resources;

import java.io.IOException;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRoleDimension.Type;
import org.batfish.role.NodeRolesData;

/**
 * The {@link NodeRoleDimensionResource} is a resource for servicing client API calls for node role
 * dimensions. It is a subresource of {@link NodeRolesResource}.
 *
 * <p>This resource provides information about the role dimension using GET. It also allows
 * modifications and creating of new dimensions using PUT.
 */
@Produces(MediaType.APPLICATION_JSON)
public class NodeRoleDimensionResource {

  private BatfishLogger _logger = Main.getLogger();
  private String _container;
  private String _dimension;

  public NodeRoleDimensionResource(String container, String dimension) {
    _container = container;
    _dimension = dimension;
  }

  @DELETE
  public Response delNodeRoleDimension() {
    _logger.infof("WMS2: delNodeRoleDimension '%s'\n", _container);
    try {
      NodeRolesData nodeRolesData = Main.getWorkMgr().getNodeRolesData(_container);
      Optional<NodeRoleDimension> dimension = nodeRolesData.getNodeRoleDimension(_dimension);
      if (!dimension.isPresent()) {
        throw new BadRequestException("Specified dimension does not exist: " + _dimension);
      }
      if (dimension.get().getType() == Type.AUTO) {
        throw new BadRequestException("Cannot delete an AUTO dimension");
      }
      nodeRolesData
          .getNodeRoleDimensions()
          .removeIf(dim -> dim.getName().equalsIgnoreCase(dimension.get().getName()));
      Main.getWorkMgr().writeNodeRoles(nodeRolesData, _container);
      return Response.ok().build();
    } catch (IOException e) {
      throw new InternalServerErrorException("Node roles resource is corrupted");
    }
  }

  @GET
  public NodeRoleDimensionBean getNodeRoleDimension() {
    _logger.infof("WMS2: getNodeRoleDimension: '%s' '%s'\n", _container, _dimension);
    try {
      NodeRoleDimensionBean bean = NodeRoleDimensionBean.create(_container, _dimension);
      if (bean == null) {
        throw new NotFoundException(
            String.format(
                "Node role dimension '%s' not found in container '%s'", _dimension, _container));
      }
      return bean;
    } catch (IOException e) {
      throw new InternalServerErrorException("Node roles resource is corrupted");
    }
  }

  // @PUT
  // public
}
