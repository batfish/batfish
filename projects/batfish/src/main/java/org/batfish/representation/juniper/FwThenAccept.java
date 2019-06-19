package org.batfish.representation.juniper;

/** Firewall filter ACCEPT action */
public enum FwThenAccept implements FwThen {
  INSTANCE;

  @Override
  public <T> T accept(FwThenVisitor<T> visitor) {
    return visitor.visitFwThenAccept(this);
  }
}
