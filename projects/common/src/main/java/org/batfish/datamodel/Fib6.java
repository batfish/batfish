package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;

/** IPv6 forwarding information base. */
public interface Fib6 extends Serializable {

  /** Return entries selected by IPv6 longest-prefix match. */
  @Nonnull
  Set<FibEntry6> get(Ip6 ip);

  /** Return all IPv6 FIB entries. */
  @Nonnull
  Set<FibEntry6> allEntries();
}
