package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** An interface network to be advertised into OSPF. */
public final class OspfNetwork implements Serializable {

  public OspfNetwork(long area, Prefix prefix) {
    _area = area;
    _prefix = prefix;
  }

  public long getArea() {
    return _area;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  private final long _area;
  private final @Nonnull Prefix _prefix;
}
