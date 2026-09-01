package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;

public interface Fib extends Serializable {

  /**
   * Return a set of {@link FibEntry fib entries} that match a given IP (using longest prefix match)
   */
  @Nonnull
  Set<FibEntry> get(Ip ip);

  /** Return the set of all entries */
  @Nonnull
  Set<FibEntry> allEntries();

  /**
   * Returns the IPs for which {@code prefix} is the longest match in the RIB, among prefixes of
   * forwarding routes.
   *
   * <p>{@code prefix} must be the network of a forwarding route in this FIB.
   */
  @Nonnull
  IpSpace matchingIps(Prefix prefix);
}
