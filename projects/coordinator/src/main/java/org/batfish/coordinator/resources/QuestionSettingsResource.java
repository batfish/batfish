package org.batfish.coordinator.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;

/**
 * The {@link QuestionSettingsResource} is a resource for servicing client API calls for a portion
 * of settings for a particular question class. It is a subresource of {@link
 * NetworkSettingsResource}.
 */
@ParametersAreNonnullByDefault
@Produces(MediaType.APPLICATION_JSON)
public class QuestionSettingsResource {

  private static final String PARAM_JSON_PATH = "jsonpath";

  private final String _network;

  private final String _questionClassId;

  public QuestionSettingsResource(String network, String questionName) {
    _network = network;
    _questionClassId = questionName;
  }

  @GET
  public Response getQuestionSettings() {
    String settings;
    try {
      settings =
          Main.getWorkMgr()
              .getQuestionSettings(_network, _questionClassId.toLowerCase(), ImmutableList.of());
    } catch (IOException e) {
      throw new InternalServerErrorException(
          String.format(
              "Failed to load question settings for network '%s', class '%s'",
              _network, _questionClassId),
          e);
    }
    return settings != null
        ? Response.ok(settings).build()
        : Response.status(Status.NOT_FOUND).build();
  }

  @Path("/{" + PARAM_JSON_PATH + ":.*}")
  public QuestionSettingsJsonPathResource getQuestionSettingsJsonResource(
      @PathParam(PARAM_JSON_PATH) String jsonPath) {
    return new QuestionSettingsJsonPathResource(_network, _questionClassId, jsonPath);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putQuestionSettings(JsonNode settings) {
    try {
      Main.getWorkMgr()
          .writeQuestionSettings(_network, _questionClassId, ImmutableList.of(), settings);
    } catch (IOException e) {
      throw new InternalServerErrorException(
          String.format(
              "Failed to write question settings for network '%s', questionClassId '%s'",
              _network, _questionClassId),
          e);
    }
    return Response.ok().build();
  }
}
