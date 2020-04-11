package org.batfish.coordinator.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
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

  static void checkQuestionExists(String network, @Nullable String analysis, String question) {
    if (!Main.getWorkMgr().checkQuestionExists(network, question, analysis)) {
      throw new NotFoundException(
          String.format(
              "Question '%s' does not exist for network: %s and analysis: %s",
              question, network, analysis));
    }
  }

  @DELETE
  public @Nonnull Response deleteQuestion() {
    if (!Main.getWorkMgr().delQuestion(_network, _questionName, _analysis)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  @Path("/answer")
  public AnswerResource getAnswer() {
    checkQuestionExists(_network, _analysis, _questionName);
    return new AnswerResource(_network, _analysis, _questionName);
  }

  @GET
  public @Nonnull Response getQuestion() throws IOException {
    String result = Main.getWorkMgr().getQuestion(_network, _questionName, _analysis);
    if (result == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(result).build();
  }

  @PUT
  public @Nonnull Response putQuestion(String questionJson) throws IOException {
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
