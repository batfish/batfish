package org.batfish.coordinator.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.WorkMgr;

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
  private String _apiKey;

  public ContainerResource(UriInfo uriInfo, String apiKey, String name) {
    _apiKey = apiKey;
    _name = name;
  }

  /** Returns information about the given {@link Container}, provided this user can access it. */
  @GET
  public Response getContainer() {
    _logger.infof("WMS2: getContainer '%s'\n", _name);
    checkAccessToContainer();
    Container container = Main.getWorkMgr().getContainer(_name);
    return Response.ok(container).build();
  }

  /** Delete a specified container with name: {@link #_name}. */
  @DELETE
  public Response deleteContainer() {
    _logger.infof("WMS2: delContainer '%s'\n", _name);
    checkAccessToContainer();
    if (WorkMgr.delContainer(_name)) {
      return Response.noContent().build();
    } else {
      return Response.serverError().build();
    }
  }

  /** Check the container {@link #_name} exists and {@link #_apiKey} has accessibility to it. */
  private void checkAccessToContainer() {
    if (!WorkMgr.checkContainerExists(_name)) {
      throw new NotFoundException(String.format("Container '%s' does not exist", _name));
    }

    if (!Main.getAuthorizer().isAccessibleContainer(_apiKey, _name, false)) {
      throw new ForbiddenException(
          String.format("container '%s' is not accessible by the api key: %s", _name, _apiKey));
    }
  }
}
