package org.batfish.coordinator.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;

/** Resource for handling requests about a specific ad-hoc or analysis question */
@ParametersAreNonnullByDefault
public final class QuestionResource {

  private final String _analysis;

  private final String _network;

  private final String _questionName;

  public QuestionResource(String network, @Nullable String analysis, String questionName) {
    _analysis = analysis;
    _network = network;
    _questionName = questionName;
  }

  @DELETE
  public @Nonnull Response deleteQuestion() {
    if (!Main.getWorkMgr().delQuestion(_network, _questionName, _analysis)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  @GET
  public @Nonnull Response getQuestion() {
    String result = Main.getWorkMgr().getQuestion(_network, _questionName, _analysis);
    if (result == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(result).build();
  }

  @PUT
  public @Nonnull Response putQuestion(String questionJson) {
    if (_analysis != null) {
      Main.getWorkMgr()
          .configureAnalysis(
              _network,
              false,
              _analysis,
              ImmutableMap.of(_questionName, questionJson),
              ImmutableList.of(),
              false);
    } else if (!Main.getWorkMgr().uploadQuestion(_network, _questionName, questionJson)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }
}
