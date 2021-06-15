package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.Nullable;

public abstract class BgpAggregateNetwork implements Serializable {

  private boolean _asSet;

  private @Nullable String _routePolicy;

  private boolean _summaryOnly;

  @Override
  public abstract boolean equals(Object o);

  public boolean getAsSet() {
    return _asSet;
  }

  public @Nullable String getRoutePolicy() {
    return _routePolicy;
  }

  public boolean getSummaryOnly() {
    return _summaryOnly;
  }

  @Override
  public abstract int hashCode();

  public void setAsSet(boolean asSet) {
    _asSet = asSet;
  }

  public void setRoutePolicy(@Nullable String attributeMap) {
    _routePolicy = attributeMap;
  }

  public void setSummaryOnly(boolean summaryOnly) {
    _summaryOnly = summaryOnly;
  }
}
