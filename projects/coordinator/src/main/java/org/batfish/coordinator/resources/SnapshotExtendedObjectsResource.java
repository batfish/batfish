package org.batfish.coordinator.resources;

import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * This resource provided functionality for storing and retrieving user-defined data at the snapshot
 * level.
 */
@ParametersAreNonnullByDefault
public final class SnapshotExtendedObjectsResource {

  private final String _network;

  private final String _snapshot;

  public SnapshotExtendedObjectsResource(String network, String snapshot) {
    _network = network;
    _snapshot = snapshot;
  }

  @Path("/{objectPath:.*}")
  public SnapshotExtendedObjectResource getSnapshotExtendedObjectResource(
      @PathParam("objectPath") String uriStr) {
    try {
      return new SnapshotExtendedObjectResource(
          _network, _snapshot, new URI("file", null, String.format("/%s", uriStr), null, null));
    } catch (URISyntaxException e) {
      throw new BadRequestException("Invalid URI", e);
    }
  }
}
