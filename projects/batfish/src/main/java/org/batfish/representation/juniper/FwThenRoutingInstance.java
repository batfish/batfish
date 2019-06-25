package org.batfish.representation.juniper;

/** Do the lookup in another routing instance */
public final class FwThenRoutingInstance implements FwThen {

  private final String _instanceName;

  public FwThenRoutingInstance(String instanceName) {
    _instanceName = instanceName;
  }

  public String getInstanceName() {
    return _instanceName;
  }

  @Override
  public <T> T accept(FwThenVisitor<T> visitor) {
    return visitor.visitThenRoutingInstance(this);
  }
}
