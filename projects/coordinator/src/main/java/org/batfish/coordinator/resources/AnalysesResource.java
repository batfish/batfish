package org.batfish.coordinator.resources;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.AnalysisMetadataMgr.AnalysisType;
import org.batfish.coordinator.Main;

@ParametersAreNonnullByDefault
@Produces(MediaType.APPLICATION_JSON)
public final class AnalysesResource {

  private final String _network;

  public AnalysesResource(String network) {
    _network = network;
  }

  @Path("/{analysis}")
  public @Nonnull AnalysisResource getAnalysisResource(@PathParam("analysis") String analysis) {
    return new AnalysisResource(_network, analysis);
  }

  @GET
  public @Nonnull Response listAnalyses() {
    SortedSet<String> result = Main.getWorkMgr().listAnalyses(_network, AnalysisType.ALL);
    if (result == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(result).build();
  }
}
