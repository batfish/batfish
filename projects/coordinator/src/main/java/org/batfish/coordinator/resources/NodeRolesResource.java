package org.batfish.coordinator.resources;

import java.io.IOException;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.coordinator.Main;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRoleDimension.Type;
import org.batfish.role.NodeRolesData;

/**
 * The {@link NodeRolesResource} is a resource for servicing client API calls for node roles. It is
 * a subresource of {@link ContainerResource}.
 *
 * <p>This resource provides information about the roles of a container using GET.
 */
@Produces(MediaType.APPLICATION_JSON)
public class NodeRolesResource {

  private BatfishLogger _logger = Main.getLogger();
  private String _container;
  private UriInfo _uriInfo;

  public NodeRolesResource(UriInfo uriInfo, String container) {
    _container = container;
    _uriInfo = uriInfo;
  }

  @POST
  public Response addNodeRoleDimension(NodeRoleDimensionBean dimBean) {
    _logger.infof("WMS2: addNodeRoleDimension '%s'\n", _container);
    if (dimBean.name == null) {
      throw new BadRequestException("Node role dimension must have a name");
    }
    if (dimBean.type == Type.AUTO) {
      throw new BadRequestException("Cannot create an AUTO dimension");
    }
    if (dimBean.name.startsWith(NodeRoleDimension.AUTO_DIMENSION_PREFIX)) {
      throw new BadRequestException(
          "Custom node role dimension name cannot start with "
              + NodeRoleDimension.AUTO_DIMENSION_PREFIX);
    }
    try {
      NodeRolesData nodeRolesData = Main.getWorkMgr().getNodeRolesData(_container);
      Optional<NodeRoleDimension> dimension = nodeRolesData.getNodeRoleDimension(dimBean.name);
      if (dimension.isPresent()) {
        throw new BadRequestException("Duplicate dimension specified: " + dimBean.name);
      }
      nodeRolesData.getNodeRoleDimensions().add(dimBean.toNodeRoleDimension());
      NodeRolesData.write(nodeRolesData, Main.getWorkMgr().getNodeRolesPath(_container));
      return Response.ok().build();
    } catch (IOException e) {
      throw new InternalServerErrorException("Node roles resource is corrupted");
    }
  }

  /** Relocate the request to {@link NodeRoleDimensionResource}. */
  @Path("/{dimension}")
  public NodeRoleDimensionResource getNodeRoleDimensionResource(
      @PathParam("dimension") String dimension) {
    return new NodeRoleDimensionResource(_uriInfo, _container, dimension);
  }

  /** Returns information about node roles in the container */
  @GET
  public NodeRolesDataBean getNodeRoles() {
    _logger.infof("WMS2: getNodeRoles '%s'\n", _container);
    try {
      return NodeRolesDataBean.create(_container);
    } catch (IOException e) {
      throw new InternalServerErrorException("Node roles resource is corrupted");
    }
  }
}
