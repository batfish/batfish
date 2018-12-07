package org.batfish.coordinator.resources;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FilenameUtils;
import org.batfish.common.CoordConsts;
import org.batfish.coordinator.Main;

/**
 * This resource provided functionality for storing and retrieving user-submitted input data at the
 * snapshot level.
 */
@ParametersAreNonnullByDefault
public class SnapshotInputObjectsResource {

  @VisibleForTesting static final String QP_KEY = "key";

  private final String _network;

  private final String _snapshot;

  public SnapshotInputObjectsResource(String network, String snapshot) {
    _network = network;
    _snapshot = snapshot;
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @SuppressWarnings("MustBeClosedChecker") // Response eventually closes it.
  public Response get(@QueryParam(QP_KEY) String key) throws IOException {
    InputStream inputStream = Main.getWorkMgr().getSnapshotInputObject(_network, _snapshot, key);
    if (inputStream == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    String filename = Paths.get(FilenameUtils.separatorsToSystem(key)).getFileName().toString();
    return Response.ok(inputStream, MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
        .header(CoordConsts.SVC_FILENAME_HDR, filename)
        .build();
  }
}
