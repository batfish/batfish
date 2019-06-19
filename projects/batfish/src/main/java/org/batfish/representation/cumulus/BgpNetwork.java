package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** A network to be exported over BGP. */
public class BgpNetwork implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull Prefix _network;

  public BgpNetwork(Prefix network) {
    _network = network;
  }

  public @Nonnull Prefix getNetwork() {
    return _network;
  }
}
