package org.batfish.coordinator.resources;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/** Resource for servicing snapshot-related client API calls at snapshot-global level */
@ParametersAreNonnullByDefault
public final class SnapshotsResource {

  private final String _network;

  public SnapshotsResource(String network) {
    _network = network;
  }

  @Path("/{snapshotParam}")
  public SnapshotResource getSnapshotResource(@PathParam("snapshotParam") String snapshot) {
    return new SnapshotResource(_network, snapshot);
  }
}
