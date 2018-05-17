package org.batfish.datamodel;

public interface HeaderSpaceConstraint {
  <T> T accept(HeaderSpaceConstraintVisitor<T> visitor);
}
