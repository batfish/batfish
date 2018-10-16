package org.batfish.coordinator.resources;

import static org.batfish.common.CoordConstsV2.RSC_QUESTIONS;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;

@ParametersAreNonnullByDefault
public final class AnalysisResource {

  private final String _analysis;
  private final String _network;

  public AnalysisResource(String network, String analysis) {
    _network = network;
    _analysis = analysis;
  }

  @DELETE
  public Response deleteAnalysis() {
    if (!Main.getWorkMgr().delAnalysis(_network, _analysis)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().build();
  }

  @Path(RSC_QUESTIONS)
  public @Nonnull QuestionsResource getAnalysisQuestionsResource() {
    return new QuestionsResource(_network, _analysis);
  }
}
