package org.batfish.representation.juniper;

public enum FwThenNop implements FwThen {
  INSTANCE;

  @Override
  public <T> T accept(FwThenVisitor<T> visitor) {
    return visitor.visitFwThenNop(this);
  }
}
