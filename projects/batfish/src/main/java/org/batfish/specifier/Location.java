package org.batfish.specifier;

public interface Location {
  <T> T accept(LocationVisitor<T> visitor);
}
