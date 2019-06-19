package org.batfish.representation.juniper;

public enum FwThenNextTerm implements FwThen {
  INSTANCE;

  @Override
  public <T> T accept(FwThenVisitor<T> visitor) {
    return visitor.visitFwThenNextTerm(this);
  }
}
