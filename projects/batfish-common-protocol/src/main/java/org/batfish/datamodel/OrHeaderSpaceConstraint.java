package org.batfish.datamodel;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public class OrHeaderSpaceConstraint implements HeaderSpaceConstraint {

  private final List<HeaderSpaceConstraint> _disjuncts;

  public OrHeaderSpaceConstraint(HeaderSpaceConstraint... conjuncts) {
    _disjuncts = ImmutableList.copyOf(conjuncts);
  }

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitOrHeaderSpaceConstraint(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrHeaderSpaceConstraint that = (OrHeaderSpaceConstraint) o;
    return Objects.equals(_disjuncts, that._disjuncts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_disjuncts);
  }
}
