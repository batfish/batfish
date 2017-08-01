package org.batfish.coordinator;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.Version;
import org.batfish.coordinator.resources.ContainerResource;
import org.codehaus.jettison.json.JSONObject;

/**
 * The Work Manager is a RESTful service for servicing client API calls.
 *
 * <p>The Work Manager provides information about the containers, testrigs, questions, and analyses
 * in the Batfish service based on the credentials of the client, and provides authenticated clients
 * the ability to create/delete or otherwise modify the same.
 */
@Path(CoordConsts.SVC_CFG_WORK_MGR2)
@Produces(MediaType.APPLICATION_JSON)
public class WorkMgrServiceV2 {

  BatfishLogger _logger = Main.getLogger();

  @DefaultValue(CoordConsts.DEFAULT_API_KEY)
  @HeaderParam(CoordConsts.SVC_KEY_API_KEY)
  private String _apiKey;

  /** Information on the URI of a request, injected by the server framework at runtime. */
  @Context private UriInfo _uriInfo;

  /** Returns the list of {@link Container containers} that the given API key may access. */
  @GET
  @Path("/containers")
  public Response getContainers() {
    _logger.info("WMS:getContainers\n");
    SortedSet<String> containerNames = Main.getWorkMgr().listContainers(_apiKey);
    List<Container> containers = new ArrayList<>();
    for (String name : containerNames) {
      String testrigUrl =
          _uriInfo.getRequestUri().resolve("container/" + name + "/testrigs").toString();
      containers.add(Container.makeContainer(name, testrigUrl));
    }
    return Response.ok(containers).build();
  }

  /** Redirect to /containers if the user does not supply a container ID. */
  @GET
  @Path("/container")
  public Response redirectContainer() {
    _logger.info("WMS:redirect container\n");
    return Response.status(Status.MOVED_PERMANENTLY)
        .location(_uriInfo.getRequestUri().resolve("containers"))
        .build();
  }

  /** Get information about the Batfish environment and supported methods. */
  @GET
  @Path("/info")
  public Response getInfo() {
    _logger.info("WMS:getInfo\n");
    try {
      JSONObject map = new JSONObject();
      map.put("Service name", "Batfish coordinator");
      map.put(CoordConsts.SVC_KEY_VERSION, Version.getVersion());
      map.put("APIs", "Enter ../application.wadl (relative to your URL) to see supported methods");
      return Response.ok(map.toString()).build();
    } catch (Exception e) {
      String stackTrace = ExceptionUtils.getFullStackTrace(e);
      _logger.error("WMS:getInfo exception: " + stackTrace);
      return Response.status(Status.EXPECTATION_FAILED).entity(e.getMessage()).build();
    }
  }

  /** Get status of the workQueue. */
  @GET
  @Path("/status")
  public Response getStatus() {
    try {
      _logger.info("WMS:getWorkQueueStatus\n");
      JSONObject retObject = Main.getWorkMgr().getStatusJson();
      retObject.put("service-version", Version.getVersion());
      return Response.ok(retObject.toString()).build();
    } catch (Exception e) {
      String stackTrace = ExceptionUtils.getFullStackTrace(e);
      _logger.error("WMS:getWorkQueueStatus exception: " + stackTrace);
      return Response.status(Status.EXPECTATION_FAILED).entity(e.getMessage()).build();
    }
  }

  /** Relocate the request to ContainerResource. */
  @Path("/container/{id}")
  public ContainerResource getResource(@PathParam("id") String id) {
    _logger.info("Relocate the request to ContainerResource");
    return new ContainerResource(_uriInfo, _apiKey, id);
  }

}
