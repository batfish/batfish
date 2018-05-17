package org.batfish.datamodel;

public class UniverseHeaderSpaceConstraint implements HeaderSpaceConstraint {
  public static final UniverseHeaderSpaceConstraint INSTANCE = new UniverseHeaderSpaceConstraint();

  private UniverseHeaderSpaceConstraint() {}

  @Override
  public <T> T accept(HeaderSpaceConstraintVisitor<T> visitor) {
    return visitor.visitTrueHeaderSpaceConstraint();
  }
}
