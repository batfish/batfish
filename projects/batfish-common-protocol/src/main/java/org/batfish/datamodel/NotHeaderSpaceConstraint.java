package org.batfish.datamodel;

import java.util.Objects;

public class NotHeaderSpaceConstraint implements HeaderSpaceConstraint {
  private final HeaderSpaceConstraint _constraint;

  public NotHeaderSpaceConstraint(HeaderSpaceConstraint constraint) {
    _constraint = constraint;
  }

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitNotHeaderSpaceConstraint(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotHeaderSpaceConstraint that = (NotHeaderSpaceConstraint) o;
    return Objects.equals(_constraint, that._constraint);
  }

  @Override
  public int hashCode() {

    return Objects.hash(_constraint);
  }
}
