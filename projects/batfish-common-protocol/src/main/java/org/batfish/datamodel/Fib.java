package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.ip.Ip;
import org.batfish.common.ip.IpSpace;
import org.batfish.common.ip.Prefix;

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
   * Returns a mapping from prefixes of forwarding routes in the RIB to the IPs for which that
   * prefix is the longest match in the RIB (among prefixes of forwarding routes).
   */
  @Nonnull
  Map<Prefix, IpSpace> getMatchingIps();
}
