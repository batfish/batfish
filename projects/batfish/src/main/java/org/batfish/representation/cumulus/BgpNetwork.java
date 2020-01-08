package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** A network to be exported over BGP. */
public class BgpNetwork implements Serializable {

  private final @Nonnull Prefix _network;

  public BgpNetwork(Prefix network) {
    _network = network;
  }

  public @Nonnull Prefix getNetwork() {
    return _network;
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
    return _network.equals(that._network);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_network);
  }
}
