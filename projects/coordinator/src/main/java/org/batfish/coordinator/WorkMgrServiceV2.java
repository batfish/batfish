package org.batfish.coordinator;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.common.CoordConstsV2.QP_VERBOSE;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Container;
import org.batfish.common.CoordConsts;
import org.batfish.common.CoordConstsV2;
import org.batfish.coordinator.resources.NetworkResource;
import org.batfish.version.Versioned;

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
  @HeaderParam(CoordConstsV2.HTTP_HEADER_BATFISH_APIKEY)
  private String _apiKey;

  /** Information on the URI of a request, injected by the server framework at runtime. */
  @Context private UriInfo _uriInfo;

  /**
   * Returns the list of {@link Container containers} that the given API key may access. Deprecated
   * in favor of {@link #getNetworks()} getNetworks}.
   */
  @GET
  @Path(CoordConstsV2.RSC_CONTAINERS)
  @Deprecated
  public Response getContainers() {
    return getNetworks();
  }

  /** Returns the list of {@link Container networks} that the given API key may access. */
  @GET
  @Path(CoordConstsV2.RSC_NETWORKS)
  public Response getNetworks() {
    _logger.info("WMS2:getNetworks\n");
    List<Container> containers = Main.getWorkMgr().getContainers(_apiKey);
    return Response.ok(containers).build();
  }

  @GET
  @Path(CoordConstsV2.RSC_QUESTION_TEMPLATES)
  public @Nonnull Response getQuestionTemplates(@QueryParam(QP_VERBOSE) boolean verbose) {
    Map<String, String> questionTemplates = Main.getQuestionTemplates(verbose);
    checkNotNull(questionTemplates, "Question templates not configured");
    return Response.ok().entity(questionTemplates).build();
  }

  /**
   * Redirect to /networks if the user does not supply a network ID. Deprecated in favor of {@link
   * #redirectNetwork() redirectNetwork}.
   */
  @GET
  @Path(CoordConstsV2.RSC_CONTAINER)
  @Deprecated
  public Response redirectContainer() {
    return redirectNetwork();
  }

  /** Redirect to /networks if the user does not supply a network ID. */
  @GET
  @Path(CoordConstsV2.RSC_NETWORK)
  public Response redirectNetwork() {
    _logger.info("WMS2:redirect network\n");
    return Response.status(Status.MOVED_PERMANENTLY)
        .location(_uriInfo.getRequestUri().resolve(CoordConstsV2.RSC_NETWORKS))
        .build();
  }

  /**
   * Relocate the request to ContainerResource. Deprecated in favor of {@link
   * #getNetworkResource(String)} getNetworkResource}.
   */
  @Path(CoordConstsV2.RSC_CONTAINERS + "/{id}")
  @Deprecated
  public NetworkResource getContainerResource(@PathParam("id") String id) {
    return getNetworkResource(id);
  }

  /** Relocate the request to ContainerResource. */
  @Path(CoordConstsV2.RSC_NETWORKS + "/{id}")
  public NetworkResource getNetworkResource(@PathParam("id") String id) {
    return new NetworkResource(_apiKey, id);
  }

  /** Handle request for component versions */
  @GET
  @Path(CoordConstsV2.RSC_VERSION)
  public Response getVersion() {
    return Response.ok().entity(Versioned.getVersions()).build();
  }
}
