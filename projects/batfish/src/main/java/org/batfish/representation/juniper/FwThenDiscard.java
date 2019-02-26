package org.batfish.representation.juniper;

/** Firewall filter DROP action */
public enum FwThenDiscard implements FwThen {
  INSTANCE;

  @Override
  public <T> T accept(FwThenVisitor<T> visitor) {
    return visitor.visitFwThenDiscard(this);
  }
}
