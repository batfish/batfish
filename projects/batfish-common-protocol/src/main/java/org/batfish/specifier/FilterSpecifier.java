package org.batfish.specifier;

import java.util.Set;
import org.batfish.datamodel.IpAccessList;

/**
 * An abstract specification of a set of filters in the network.
 *
 * <p>TODO: Only IPv4 filters are supported at the moment; extend to IPv6 filters.
 */
public interface FilterSpecifier {
  /**
   * Returns the filters on {@code node} that match this specifier.
   *
   * @param ctxt Information about the network that may be used to determine match.
   */
  Set<IpAccessList> resolve(String node, SpecifierContext ctxt);
}
