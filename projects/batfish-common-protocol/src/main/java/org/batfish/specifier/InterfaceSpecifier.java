package org.batfish.specifier;

import java.util.Set;
import org.batfish.datamodel.Interface;

/** An abstract specification of a set of interfaces in the network. */
public interface InterfaceSpecifier {
  /**
   * Returns the interfaces on {@code nodes} that match this specifier.
   *
   * @param ctxt Information about the network that may be used to determine match.
   */
  Set<Interface> resolve(Set<String> nodes, SpecifierContext ctxt);
}
