package org.batfish.coordinator.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;

/** Resource for interacting with ad-hoc or analysis questions for a given network */
@ParametersAreNonnullByDefault
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class QuestionsResource {

  private final String _analysis;
  private final String _network;

  /**
   * Question resource for a given network and analysis. If analysis is {@code null}, resource is
   * for interacting with ad-hoc questions for the network.
   */
  public QuestionsResource(String network, @Nullable String analysis) {
    _network = network;
    _analysis = analysis;
  }

  @Path("/{question}")
  @DELETE
  public Response deleteQuestion(@PathParam("question") String question) {
    if (!Main.getWorkMgr().delQuestion(_network, question, _analysis)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  @Path("/{question}")
  @GET
  public Response getQuestion(@PathParam("question") String question) {
    String result = Main.getWorkMgr().getQuestion(_network, question, _analysis);
    if (result == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(result).build();
  }

  @GET
  public Response getQuestions() {
    SortedSet<String> result =
        _analysis != null
            ? Main.getWorkMgr().listAnalysisQuestions(_network, _analysis)
            : Main.getWorkMgr().listQuestions(_network, false);
    if (result == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(result).build();
  }

  @Path("/{question}")
  @PUT
  public Response putQuestion(@PathParam("question") String question, String questionJson) {
    if (_analysis != null) {
      Main.getWorkMgr()
          .configureAnalysis(
              _network,
              false,
              _analysis,
              ImmutableMap.of(question, questionJson),
              ImmutableList.of(),
              false);
    } else if (!Main.getWorkMgr().uploadQuestion(_network, question, questionJson)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }
}
