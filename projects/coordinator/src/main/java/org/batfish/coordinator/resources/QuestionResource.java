package org.batfish.coordinator.resources;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;

/** Resource for handling requests about a specific ad-hoc question */
@ParametersAreNonnullByDefault
public final class QuestionResource {

  private final String _network;

  private final String _questionName;

  public QuestionResource(String network, String questionName) {
    _network = network;
    _questionName = questionName;
  }

  static void checkQuestionExists(String network, String question) {
    if (!Main.getWorkMgr().checkQuestionExists(network, question)) {
      throw new NotFoundException(
          String.format("Question '%s' does not exist for network: %s", question, network));
    }
  }

  @DELETE
  public @Nonnull Response deleteQuestion() {
    if (!Main.getWorkMgr().delQuestion(_network, _questionName)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  @Path("/answer")
  public AnswerResource getAnswer() {
    checkQuestionExists(_network, _questionName);
    return new AnswerResource(_network, _questionName);
  }

  @GET
  public @Nonnull Response getQuestion() throws IOException {
    String result = Main.getWorkMgr().getQuestion(_network, _questionName);
    if (result == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(result).build();
  }

  @PUT
  public @Nonnull Response putQuestion(String questionJson) throws IOException {
    if (!Main.getWorkMgr().uploadQuestion(_network, _questionName, questionJson)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }
}
