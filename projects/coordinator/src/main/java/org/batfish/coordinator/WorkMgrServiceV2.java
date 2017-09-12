package org.batfish.coordinator;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.resources.ContainerResource;
import org.batfish.datamodel.pojo.CreateContainerRequest;

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

  private BatfishLogger _logger = Main.getLogger();

  @DefaultValue(CoordConsts.DEFAULT_API_KEY)
  @HeaderParam(CoordConsts.SVC_KEY_API_KEY)
  private String _apiKey;

  /** Information on the URI of a request, injected by the server framework at runtime. */
  @Context private UriInfo _uriInfo;

  /** Returns the list of {@link Container containers} that the given API key may access. */
  @GET
  @Path(CoordConsts.SVC_KEY_CONTAINERS)
  public Response getContainers() {
    _logger.info("WMS:getContainers\n");
    List<Container> containers = Main.getWorkMgr().getContainers(_apiKey);
    return Response.ok(containers).build();
  }

  /** Redirect to /containers if the user does not supply a container ID. */
  @GET
  @Path(CoordConsts.SVC_KEY_CONTAINER_NAME)
  public Response redirectContainer() {
    _logger.info("WMS:redirect container\n");
    return Response.status(Status.MOVED_PERMANENTLY)
        .location(_uriInfo.getRequestUri().resolve(CoordConsts.SVC_KEY_CONTAINERS))
        .build();
  }

  /** Create a new container using request body. */
  @POST
  @Path(CoordConsts.SVC_KEY_CONTAINER_NAME)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createContainer(String createRequest) {
    _logger.info("WMS:create container\n");
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    CreateContainerRequest request;
    try {
      request = mapper.readValue(createRequest, new TypeReference<CreateContainerRequest>() {});
    } catch (IOException e) {
      throw new BadRequestException("The input JSON is not property formatted", e);
    }
    String outputContainerName;
    if (request.getSetName()) {
      outputContainerName = Main.getWorkMgr().initContainer(request.getName(), null);
    } else {
      outputContainerName = Main.getWorkMgr().initContainer(null, request.getName());
    }
    Main.getAuthorizer().authorizeContainer(_apiKey, outputContainerName);
    return Response.created(
            _uriInfo
                .getBaseUri()
                .resolve(Paths.get(CoordConsts.SVC_KEY_CONTAINERS, outputContainerName).toString()))
        .build();
  }

  /** Relocate the request to ContainerResource. */
  @Path("/containers/{id}")
  public ContainerResource getResource(@PathParam("id") String id) {
    _logger.info("Relocate the request to ContainerResource");
    return new ContainerResource(_uriInfo, _apiKey, id);
  }
}
