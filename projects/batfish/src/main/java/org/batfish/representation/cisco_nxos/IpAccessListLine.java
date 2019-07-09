package org.batfish.representation.cisco_nxos;

import java.io.Serializable;

/** A line of an {@link IpAccessList}. */
public abstract class IpAccessListLine implements Serializable {

  private final long _line;

  protected IpAccessListLine(long line) {
    _line = line;
  }

  public abstract <T> T accept(IpAccessListLineVisitor<T> visitor);

  public long getLine() {
    return _line;
  }
}
