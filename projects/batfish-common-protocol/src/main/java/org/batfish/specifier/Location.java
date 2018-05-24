package org.batfish.specifier;

/**
 * Identifies a single location in the network -- an VRF, an interface, the link of an interface,
 * etc. Locations are pure data -- they should have no behavior other than the accept method that
 * allows them to be inspected.
 */
public interface Location {
  <T> T accept(LocationVisitor<T> visitor);
}
