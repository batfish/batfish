package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpWildcard;

/** A line of an {@link ObjectGroupIpAddress}. */
public final class ObjectGroupIpAddressLine implements Serializable {

  public ObjectGroupIpAddressLine(long line, IpWildcard ipWildcard) {
    _line = line;
    _ipWildcard = ipWildcard;
  }

  public @Nonnull IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  public long getLine() {
    return _line;
  }

  private final @Nonnull IpWildcard _ipWildcard;
  private final long _line;
}
