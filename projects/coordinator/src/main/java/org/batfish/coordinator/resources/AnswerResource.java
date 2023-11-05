package org.batfish.coordinator.resources;

import static org.batfish.common.CoordConstsV2.RSC_FILTER;

import java.io.IOException;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;
import org.batfish.datamodel.answers.Answer;

/** Resource for handling requests about a specific ad-hoc question's answer */
@ParametersAreNonnullByDefault
public final class AnswerResource {

  private final String _network;

  private final String _questionName;

  public AnswerResource(String network, String questionName) {
    _network = network;
    _questionName = questionName;
  }

  /**
   * Get the answer for the question, with the specified filtering options applied
   *
   * @param filterAnswerBean The {@link FilterAnswerBean} containing parameters to fetch and filter
   *     the answer
   * @return Response containing JSON serialized {@link Answer} object
   */
  @POST
  @Path(RSC_FILTER)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response filterAnswer(FilterAnswerBean filterAnswerBean) throws IOException {
    if (!Main.getWorkMgr().checkSnapshotExists(_network, filterAnswerBean.snapshot)) {
      return Response.status(Status.NOT_FOUND)
          .entity(
              String.format(
                  "Snapshot %s not found in network %s", filterAnswerBean.snapshot, _network))
          .build();
    }
    Answer ans =
        Main.getWorkMgr()
            .getAnswer(
                _network,
                filterAnswerBean.snapshot,
                _questionName,
                filterAnswerBean.referenceSnapshot);
    if (ans == null) {
      return Response.status(Status.NOT_FOUND)
          .entity(
              String.format(
                  "Answer not found for question %s on network: %s, snapshot: %s,"
                      + " referenceSnapshot: %s",
                  _questionName,
                  _network,
                  filterAnswerBean.snapshot,
                  filterAnswerBean.referenceSnapshot))
          .build();
    }

    // Filter the resulting answer
    return Response.ok()
        .entity(Main.getWorkMgr().filterAnswer(ans, filterAnswerBean.filterOptions))
        .build();
  }

  /**
   * Get the answer for the specified question, regarding the specified {@code snapshot} and
   * optionally {@code referenceSnapshot}.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAnswer(
      @QueryParam("snapshot") @Nullable String snapshot,
      @QueryParam("referenceSnapshot") @Nullable String referenceSnapshot)
      throws IOException {
    if (snapshot == null || snapshot.isEmpty()) {
      return Response.status(Status.BAD_REQUEST)
          .entity("Snapshot must be specified to fetch question answer")
          .build();
    }
    if (!Main.getWorkMgr().checkSnapshotExists(_network, snapshot)) {
      return Response.status(Status.NOT_FOUND)
          .entity(String.format("Snapshot %s not found in network %s", snapshot, _network))
          .build();
    }
    Answer ans = Main.getWorkMgr().getAnswer(_network, snapshot, _questionName, referenceSnapshot);
    if (ans == null) {
      return Response.status(Status.NOT_FOUND)
          .entity(
              String.format(
                  "Answer not found for question %s on network: %s, snapshot: %s,"
                      + " referenceSnapshot: %s",
                  _questionName, _network, snapshot, referenceSnapshot))
          .build();
    }
    return Response.ok().entity(ans).build();
  }
}
