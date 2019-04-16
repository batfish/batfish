package org.batfish.coordinator.resources;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;
import org.batfish.datamodel.answers.Answer;

/** Resource for handling requests about a specific ad-hoc or analysis question's answer */
@ParametersAreNonnullByDefault
public final class AnswerResource {

  private final String _analysis;

  private final String _network;

  private final String _questionName;

  public AnswerResource(String network, @Nullable String analysis, String questionName) {
    _analysis = analysis;
    _network = network;
    _questionName = questionName;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAnswer(
      @Nullable @QueryParam("snapshot") String snapshot,
      @Nullable @QueryParam("referenceSnapshot") String referenceSnapshot) {
    // return Response.ok("got answer " + snapshot).build();
    try {
      checkArgument(snapshot != null, "Snapshot must be specified to fetch question answer");
      Answer ans =
          Main.getWorkMgr()
              .getAnswer(_network, snapshot, _questionName, referenceSnapshot, _analysis);
      return Response.ok().entity(ans).type(MediaType.APPLICATION_JSON).build();
    } catch (FileNotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
    } catch (IllegalArgumentException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (IOException e) {
      // Other inputs should be validated by this point, don't expect to run into this exception
      // under normal circumstances
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }
}
