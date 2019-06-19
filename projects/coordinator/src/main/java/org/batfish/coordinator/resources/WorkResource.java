package org.batfish.coordinator.resources;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;
import org.batfish.coordinator.QueuedWork;

/** Resource for servicing client API calls for work item(s) for a given network */
@ParametersAreNonnullByDefault
public final class WorkResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{workid}")
  public @Nonnull Response getWorkStatus(@PathParam("workid") @Nonnull String workIdStr) {
    UUID workId;
    try {
      workId = UUID.fromString(workIdStr);
    } catch (IllegalArgumentException e) {
      return Response.status(Status.BAD_REQUEST).entity("Work ID must be a valid UUID").build();
    }
    QueuedWork queuedWork = Main.getWorkMgr().getWork(workId);
    if (queuedWork == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok().entity(queuedWork.toWorkStatus()).build();
  }
}
