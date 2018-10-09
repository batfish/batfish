package org.batfish.coordinator.resources;

import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * This resource provided functionality for storing and retrieving user-defined data at the network
 * level.
 */
@ParametersAreNonnullByDefault
public final class NetworkExtendedObjectsResource {

  private final String _network;

  public NetworkExtendedObjectsResource(String network) {
    _network = network;
  }

  @Path("/{objectPath:.*}")
  public NetworkExtendedObjectResource getNetworkExtendedObjectResource(
      @PathParam("objectPath") String uriStr) {
    try {
      return new NetworkExtendedObjectResource(
          _network, new URI("file", null, String.format("/%s", uriStr), null, null));
    } catch (URISyntaxException e) {
      throw new BadRequestException("Invalid URI", e);
    }
  }
}
