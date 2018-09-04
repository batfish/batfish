package org.batfish.coordinator.resources;

import static org.batfish.common.CoordConstsV2.RSC_NODE_ROLES;
import static org.batfish.common.CoordConstsV2.RSC_REFERENCE_LIBRARY;
import static org.batfish.common.CoordConstsV2.RSC_SETTINGS;

import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.coordinator.Main;

/**
 * The {@link ContainerResource} is a resource for servicing client API calls at container level.
 *
 * <p>The ContainerResource provides information about a specified container, and provides the
 * ability to delete a specified container for authenticated clients.
 *
 * <p>The ContainerResource also provides the access entry for other subResources.
 */
@Produces(MediaType.APPLICATION_JSON)
public class ContainerResource {

  private BatfishLogger _logger = Main.getLogger();

  private String _name;

  public ContainerResource(String apiKey, String name) {
    checkAccessToContainer(apiKey, name);
    _name = name;
  }

  /** Returns information about the given {@link Container}, provided this user can access it. */
  @GET
  public Response getContainer() {
    _logger.infof("WMS2: getNetwork '%s'\n", _name);
    Container container = Main.getWorkMgr().getContainer(_name);
    return Response.ok(container).build();
  }

  /** Relocate the request to {@link NodeRolesResource}. */
  @Path(RSC_NODE_ROLES)
  public NodeRolesResource getNodeRolesResource() {
    return new NodeRolesResource(_name);
  }

  /** Relocate the request to {@link ReferenceLibraryResource}. */
  @Path(RSC_REFERENCE_LIBRARY)
  public ReferenceLibraryResource getReferenceLibraryResource() {
    return new ReferenceLibraryResource(_name);
  }

  /** Relocate the request to {@link NetworkSettingsResource}. */
  @Path(RSC_SETTINGS)
  public NetworkSettingsResource getSettingsResource() {
    return new NetworkSettingsResource(_name);
  }

  /** Delete a specified container with name: {@link #_name}. */
  @DELETE
  public Response deleteContainer() {
    _logger.infof("WMS2: delNetwork '%s'\n", _name);
    if (Main.getWorkMgr().delContainer(_name)) {
      return Response.noContent().build();
    } else {
      return Response.serverError().build();
    }
  }

  /** Check if {@code container} exists and {@code apiKey} has access to it. */
  private static void checkAccessToContainer(String apiKey, String container) {
    if (!Main.getWorkMgr().checkContainerExists(container)) {
      throw new NotFoundException(String.format("Network '%s' does not exist", container));
    }

    if (!Main.getAuthorizer().isAccessibleContainer(apiKey, container, false)) {
      throw new ForbiddenException(
          String.format("network '%s' is not accessible by the api key: %s", container, apiKey));
    }
  }
}
