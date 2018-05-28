package org.batfish.coordinator.resources;

import java.io.IOException;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.Main;
import org.batfish.role.NodeRolesData;

/**
 * The {@link NodeRolesResource} is a resource for servicing client API calls for node roles. It is
 * a subresource of {@link ContainerResource}.
 *
 * <p>This resource provides information about a specified container using GET.
 */
@Produces(MediaType.APPLICATION_JSON)
public class NodeRolesResource {

  private BatfishLogger _logger = Main.getLogger();

  private String _container;

  public NodeRolesResource(UriInfo uriInfo, String container) {
    _container = container;
  }

  /** Returns information about node roles in the container */
  @GET
  public Response getNodeRoles() {
    _logger.infof("WMS2: getNodeRoles '%s'\n", _container);
    try {
      NodeRolesData nodeRolesData = Main.getWorkMgr().getNodeRolesData(_container);
      Set<String> nodes = Main.getWorkMgr().getNodes(_container);
      nodeRolesData
          .getNodeRoleDimensions()
          .forEach(dim -> dim.getRoles().forEach(role -> role.resetNodes(nodes)));
      // TODO: we shouldn't have to explicitly go through BatfishObjectMapper here. The service is
      // not properly set up to use our custom mapper
      return Response.ok(BatfishObjectMapper.mapper().valueToTree(nodeRolesData)).build();
    } catch (IOException e) {
      throw new InternalServerErrorException("Node roles resource is corrupted");
    }
  }
}
