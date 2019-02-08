package org.batfish.representation.juniper;

/** Do the lookup in another routing instance */
public final class FwThenRoutingInstance implements FwThen {

  private static final long serialVersionUID = 1L;
  private final String _instanceName;

  public FwThenRoutingInstance(String instanceName) {
    _instanceName = instanceName;
  }

  public String getInstanceName() {
    return _instanceName;
  }
}
