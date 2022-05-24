package org.batfish.coordinator.resources;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;

/** Resource for interacting with ad-hoc questions for a given network */
@ParametersAreNonnullByDefault
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class QuestionsResource {

  private final String _network;

  /** Question resource for a given network. */
  public QuestionsResource(String network) {
    _network = network;
  }

  @Path("/{question}")
  public @Nonnull QuestionResource question(@PathParam("question") String question) {
    return new QuestionResource(_network, question);
  }

  @GET
  public @Nonnull Response getQuestions() {
    SortedSet<String> result = Main.getWorkMgr().listQuestions(_network, false);
    if (result == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(result).build();
  }
}
