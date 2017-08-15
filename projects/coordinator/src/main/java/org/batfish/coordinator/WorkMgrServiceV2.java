package org.batfish.coordinator;

import static org.batfish.common.CoordConsts.DEFAULT_API_KEY;
import static org.batfish.common.CoordConsts.SVC_KEY_API_KEY;

import java.util.List;
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
import org.batfish.common.CoordConsts;
import org.batfish.datamodel.pojo.AccessLevel;
import org.batfish.datamodel.pojo.Container;

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
  /** Information on the URI of a request, injected by the server framework at runtime. */
  @Context private UriInfo _uriInfo;

  /** Returns the list of {@link Container containers} that the given API key may access. */
  @GET
  @Path("/containers")
  public Response getContainers(
      @DefaultValue(DEFAULT_API_KEY) @HeaderParam(SVC_KEY_API_KEY) String apiKey) {
    List<Container> containers = Main.getWorkMgr().listContainers(apiKey, AccessLevel.SUMMARY);
    return Response.ok(containers).build();
  }

  /** Redirect to /containers if the user does not supply a container ID. */
  @GET
  @Path("/container")
  public Response redirectContainer(
      @DefaultValue(DEFAULT_API_KEY) @HeaderParam(SVC_KEY_API_KEY) String apiKey) {
    return Response.status(Status.MOVED_PERMANENTLY)
        .location(_uriInfo.getRequestUri().resolve("containers"))
        .build();
  }

  /** Returns information about the given {@link Container}, provided this user can access it. */
  @GET
  @Path("/container/{id}")
  public Response getContainer(
      @DefaultValue(DEFAULT_API_KEY) @HeaderParam(SVC_KEY_API_KEY) String apiKey,
      @PathParam("id") String id) {
    if (!Main.getAuthorizer().isAccessibleContainer(apiKey, id, false)) {
      // TODO: put a proper error entity here.
      return Response.status(Status.NOT_FOUND).build();
    }

    Container container = Main.getWorkMgr().getContainer(id, AccessLevel.SUMMARY);
    return Response.ok(container).build();
  }
}
