package org.batfish.representation.cisco;

public abstract class RoutePolicyDeleteStatement extends RoutePolicySetStatement {

  private static final long serialVersionUID = 1L;

  public abstract RoutePolicyDeleteType getDeleteType();
}
