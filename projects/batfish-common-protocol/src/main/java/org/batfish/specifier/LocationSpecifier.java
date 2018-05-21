package org.batfish.specifier;

import java.util.Set;
import org.batfish.datamodel.IpSpace;

/** An abstract specifier of network {@link Location}s. */
public interface LocationSpecifier {
  /**
   * Resolve this specifier to a set of concrete {@link Location}s for this network.
   *
   * @param ctxt Information about the network that may be used to resolve concrete {@link
   *     IpSpace}s.
   * @return The set of concrete {@link Location}s.
   */
  Set<Location> resolve(SpecifierContext ctxt);
}
