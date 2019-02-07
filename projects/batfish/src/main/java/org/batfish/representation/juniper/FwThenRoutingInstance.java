package org.batfish.representation.juniper;

import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FwThenRoutingInstance that = (FwThenRoutingInstance) o;
    return Objects.equals(getInstanceName(), that.getInstanceName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getInstanceName());
  }
}
