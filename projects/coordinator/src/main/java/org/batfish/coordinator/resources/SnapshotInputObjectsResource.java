package org.batfish.coordinator.resources;

import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * This resource provided functionality for storing and retrieving user-submitted input data at the
 * snapshot level.
 */
@ParametersAreNonnullByDefault
public class SnapshotInputObjectsResource {

  private final String _network;

  private final String _snapshot;

  public SnapshotInputObjectsResource(String network, String snapshot) {
    _network = network;
    _snapshot = snapshot;
  }

  @Path("/{objectPath:.*}")
  public SnapshotInputObjectResource getSnapshotInputObjectResource(
      @PathParam("objectPath") String uriStr) {
    try {
      return new SnapshotInputObjectResource(
          _network, _snapshot, new URI("file", null, String.format("/%s", uriStr), null, null));
    } catch (URISyntaxException e) {
      throw new BadRequestException("Invalid URI", e);
    }
  }
}
