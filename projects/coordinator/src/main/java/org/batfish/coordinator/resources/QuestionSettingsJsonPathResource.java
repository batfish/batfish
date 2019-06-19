package org.batfish.coordinator.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;

/**
 * The {@link QuestionSettingsJsonPathResource} is a resource for servicing client API calls for a
 * subset of settings for a particular question class at a particular path in the JSON
 * representation of those settings. It is a subresource of {@link QuestionSettingsResource}.
 */
@ParametersAreNonnullByDefault
@Produces(MediaType.APPLICATION_JSON)
public class QuestionSettingsJsonPathResource {

  private static List<String> getComponents(String jsonPath) {
    ImmutableList.Builder<String> components = ImmutableList.builder();
    for (Path path = Paths.get(URI.create(jsonPath).getPath());
        path != null;
        path = path.getParent()) {
      components.add(path.getFileName().toString());
    }
    return components.build().reverse();
  }

  private final String _jsonPath;

  private final String _network;

  private final String _questionName;

  public QuestionSettingsJsonPathResource(String network, String questionName, String jsonPath) {
    _network = network;
    _questionName = questionName;
    _jsonPath = jsonPath;
  }

  @GET
  public Response getQuestionSettingsJsonPath() {
    String settings;
    try {
      settings =
          Main.getWorkMgr()
              .getQuestionSettings(_network, _questionName.toLowerCase(), getComponents(_jsonPath));
    } catch (IOException e) {
      throw new InternalServerErrorException(
          String.format(
              "Failed to load question settings for network '%s', class '%s', path '%s'",
              _network, _questionName, _jsonPath),
          e);
    }
    return settings != null
        ? Response.ok(settings).build()
        : Response.status(Status.NOT_FOUND).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putQuestionSettings(JsonNode settings) {
    try {
      Main.getWorkMgr()
          .writeQuestionSettings(
              _network, _questionName.toLowerCase(), getComponents(_jsonPath), settings);
    } catch (IOException e) {
      throw new InternalServerErrorException(
          String.format(
              "Failed to write question settings for network '%s', class '%s', path '%s'",
              _network, _questionName, _jsonPath),
          e);
    }
    return Response.ok().build();
  }
}
