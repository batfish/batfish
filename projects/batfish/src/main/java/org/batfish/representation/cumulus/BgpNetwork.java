package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** A network to be exported over BGP. */
public class BgpNetwork implements Serializable {

  private final @Nonnull Prefix _network;
  private final @Nullable String _routeMap;

  public BgpNetwork(Prefix network, @Nullable String routeMap) {
    _network = network;
    _routeMap = routeMap;
  }

  public @Nonnull Prefix getNetwork() {
    return _network;
  }

  public @Nullable String getRouteMap() {
    return _routeMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpNetwork)) {
      return false;
    }
    BgpNetwork that = (BgpNetwork) o;
    return _network.equals(that._network)
        && Objects.equals(_routeMap, that._routeMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_network, _routeMap);
  }
}
