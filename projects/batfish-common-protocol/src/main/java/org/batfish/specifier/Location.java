package org.batfish.specifier;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Identifies a single location in the network -- an VRF, an interface, the link of an interface,
 * etc. Locations are pure data -- they should have no behavior other than the accept method that
 * allows them to be inspected.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface Location {
  <T> T accept(LocationVisitor<T> visitor);
}
