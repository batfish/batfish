package org.batfish.coordinator.resources;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.common.CoordConsts;
import org.batfish.coordinator.Main;

/**
 * This resource provided functionality for storing and retrieving user-defined data at the network
 * level.
 */
@ParametersAreNonnullByDefault
public final class NetworkObjectsResource {

  @VisibleForTesting static final String QP_KEY = "key";

  private final String _network;

  public NetworkObjectsResource(String network) {
    _network = network;
  }

  @DELETE
  public @Nonnull Response delete(@QueryParam(QP_KEY) String key) throws IOException {
    if (Main.getWorkMgr().deleteNetworkObject(_network, key)) {
      return Response.ok().build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @SuppressWarnings({"MustBeClosedChecker"}) // Response eventually closes it.
  public @Nonnull Response get(@QueryParam(QP_KEY) String key) throws IOException {
    InputStream inputStream = Main.getWorkMgr().getNetworkObject(_network, key);
    if (inputStream == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    String filename = Paths.get(key).getFileName().toString();
    return Response.ok(inputStream, MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
        .header(CoordConsts.SVC_FILENAME_HDR, filename)
        .build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  public @Nonnull Response put(InputStream inputStream, @QueryParam(QP_KEY) String key)
      throws IOException {
    if (Main.getWorkMgr().putNetworkObject(inputStream, _network, key)) {
      return Response.ok().build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }
}
