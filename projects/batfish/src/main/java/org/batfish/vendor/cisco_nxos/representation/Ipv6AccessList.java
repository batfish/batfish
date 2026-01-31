package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** An access control list for IPv6 traffic */
public final class Ipv6AccessList implements Serializable {

  private @Nonnull FragmentsBehavior _fragmentsBehavior;
  private final @Nonnull String _name;

  public Ipv6AccessList(String name) {
    _name = name;
    _fragmentsBehavior = FragmentsBehavior.DEFAULT;
  }

  public @Nonnull FragmentsBehavior getFragmentsBehavior() {
    return _fragmentsBehavior;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setFragmentsBehavior(FragmentsBehavior fragmentsBehavior) {
    _fragmentsBehavior = fragmentsBehavior;
  }
}
