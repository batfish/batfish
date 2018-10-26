package org.batfish.coordinator.resources;

import static org.batfish.common.CoordConstsV2.RSC_ISSUES;
import static org.batfish.common.CoordConstsV2.RSC_QUESTIONS;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The {@link NetworkSettingsResource} is a resource for servicing client API calls for
 * network-level settings. It is a subresource of {@link NetworkResource}.
 */
@Produces(MediaType.APPLICATION_JSON)
public class NetworkSettingsResource {

  private String _network;

  public NetworkSettingsResource(String network) {
    _network = network;
  }

  /** Relocate the request to {@link IssueSettingsResource}. */
  @Path(RSC_ISSUES)
  public IssueSettingsResource getReferenceBookResource() {
    return new IssueSettingsResource(_network);
  }

  /** Nothing to send today for a plan get call on this resource */
  @GET
  public Response getNetworkSettings() {
    return Response.noContent().build();
  }

  /** Relocate the request to QuestionSettingsResource. */
  @Path(RSC_QUESTIONS + "/{questionname}")
  public QuestionSettingsResource getQuestionsSettingsResource(
      @PathParam("questionname") String questionName) {
    return new QuestionSettingsResource(_network, questionName);
  }
}
