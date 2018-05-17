package org.batfish.datamodel;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public class AndHeaderSpaceConstraint implements HeaderSpaceConstraint {

  private final List<HeaderSpaceConstraint> _conjuncts;

  public AndHeaderSpaceConstraint(Iterable<HeaderSpaceConstraint> conjuncts) {
    _conjuncts = ImmutableList.copyOf(conjuncts);
  }

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitAndHeaderSpaceConstraint(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AndHeaderSpaceConstraint that = (AndHeaderSpaceConstraint) o;
    return Objects.equals(_conjuncts, that._conjuncts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_conjuncts);
  }
}
