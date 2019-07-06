package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** An access control list for IPv4 traffic */
public final class IpAccessList implements Serializable {

  public static final int MAX_NAME_LENGTH = 64;

  private final @Nonnull SortedMap<Long, IpAccessListLine> _lines;
  private final @Nonnull String _name;

  public IpAccessList(String name) {
    _name = name;
    _lines = new TreeMap<>();
  }

  public @Nonnull SortedMap<Long, IpAccessListLine> getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
