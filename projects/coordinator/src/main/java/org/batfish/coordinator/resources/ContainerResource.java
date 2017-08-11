package org.batfish.coordinator.resources;

import java.util.SortedSet;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.coordinator.Main;

/**
 * The ContainerResource is a RESTful service for servicing client API calls at container level.
 *
 * <p>The ContainerResource provides information about a specified container, and provides the
 * ability to create/delete a specified container for authenticated clients.
 *
 * <p>The ContainerResource also provides the access entry for client API calls at testrig level.
 */
@Produces(MediaType.APPLICATION_JSON)
public class ContainerResource {

  private BatfishLogger _logger = Main.getLogger();

  private UriInfo _uriInfo;
  private String _name;
  private String _apiKey;

  public ContainerResource(UriInfo uriInfo, String apiKey, String name) {
    _uriInfo = uriInfo;
    _apiKey = apiKey;
    _name = name;
  }

  /** Returns information about the given {@link Container}, provided this user can access it. */
  @GET
  public Response getContainer() {
    _logger.info("WMS: getContainer '" + _name + "'\n");
    validate();
    Container container = Main.getWorkMgr().getContainer(_name);
    return Response.ok(container).build();
  }

  /** Initialize a new container with name: {@link #_name}. */
  @POST
  public Response initContainer() {
    _logger.info("WMS: initContainer '" + _name + "'\n");
    // TODO do we need to worry about containerPrefix here?
    String outputContainerName = Main.getWorkMgr().initContainer(_name, null);
    Main.getAuthorizer().authorizeContainer(_apiKey, outputContainerName);
    return Response.created(_uriInfo.getRequestUri()).build();
  }

  /** Delete a specified container with name: {@link #_name}. */
  @DELETE
  public Response deleteContainer() {
    _logger.info("WMS: delContainer '" + _name + "'\n");
    if (!Main.getAuthorizer().isAccessibleContainer(_apiKey, _name, false)) {
      throw new ForbiddenException(
          "container '" + _name + "' is not accessible by the api key: " + _apiKey);
    }
    Main.getWorkMgr().delContainer(_name);
    return Response.noContent().build();
  }

  /**
   * Returns the list of {@link org.batfish.common.Testrig testrigs} that the given API key may
   * access.
   */
  @GET
  @Path("/testrigs")
  public Response getTestrigs() {
    validate();
    SortedSet<String> testrigNames = Main.getWorkMgr().listTestrigs(_name);
    return Response.ok(testrigNames).build();
  }

  /** Redirect to /testrigs if the user does not supply a testrig ID. */
  @GET
  @Path("/testrig")
  public Response redirectTestrig() {
    validate();
    UriBuilder ub = _uriInfo.getBaseUriBuilder();
    ub.path(CoordConsts.SVC_CFG_WORK_MGR2).path("container").path(_name).path("testrigs");
    return Response.status(Status.MOVED_PERMANENTLY)
        .location(ub.build())
        .build();
  }

  //  /** Relocate the request to TestrigResource. */
  //  @Path("/testrig/{id}")
  //  public org.batfish.coordinator.resources.TestrigResource getResource(
  //  @PathParam("id") String id) {
  //    validate();
  //    return new TestrigResource(this._uriInfo, this._name, id);
  //  }

  /** Validates the container {@link #_name} exists and {@link #_apiKey} has accessibility to it. */
  private void validate() {
    if (!Main.getWorkMgr().checkContainerExist(_name)) {
      throw new NotFoundException("Container '" + _name + "' does not exist");
    }

    if (!Main.getAuthorizer().isAccessibleContainer(_apiKey, _name, false)) {
      throw new ForbiddenException(
          "container '" + _name + "' is not accessible by the api key: " + _apiKey);
    }
  }

}
