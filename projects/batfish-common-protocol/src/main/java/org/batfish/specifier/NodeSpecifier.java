package org.batfish.specifier;

import java.util.Set;

/** An abstract specification of a set of nodes in the network. */
public interface NodeSpecifier {
  /**
   * Resolve this specifier into a set of concrete node names.
   *
   * @param ctxt Information about the network that may be used to resolve concrete node names.
   * @return The set of concrete node names.
   */
  Set<String> resolve(SpecifierContext ctxt);
}
