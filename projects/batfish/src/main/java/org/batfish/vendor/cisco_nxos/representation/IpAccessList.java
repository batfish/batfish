package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** An access control list for IPv4 traffic */
public final class IpAccessList implements Serializable {

  private @Nonnull FragmentsBehavior _fragmentsBehavior;
  private final @Nonnull SortedMap<Long, IpAccessListLine> _lines;
  private final @Nonnull String _name;

  public IpAccessList(String name) {
    _name = name;
    _lines = new TreeMap<>();
    _fragmentsBehavior = FragmentsBehavior.DEFAULT;
  }

  public @Nonnull FragmentsBehavior getFragmentsBehavior() {
    return _fragmentsBehavior;
  }

  public @Nonnull SortedMap<Long, IpAccessListLine> getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setFragmentsBehavior(FragmentsBehavior fragmentsBehavior) {
    _fragmentsBehavior = fragmentsBehavior;
  }
}
