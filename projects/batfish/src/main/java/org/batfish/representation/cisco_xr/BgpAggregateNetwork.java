package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class BgpAggregateNetwork implements Serializable {

  private boolean _asSet;

  private @Nullable String _routePolicy;

  private boolean _summaryOnly;

  @Override
  public abstract boolean equals(@Nullable Object o);

  protected final boolean baseEquals(BgpAggregateNetwork that) {
    return _asSet == that._asSet
        && Objects.equals(_routePolicy, that._routePolicy)
        && _summaryOnly == that._summaryOnly;
  }

  protected final int baseHashcode() {
    return Objects.hash(_asSet, _routePolicy, _summaryOnly);
  }

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

  public void setRoutePolicy(@Nullable String routePolicy) {
    _routePolicy = routePolicy;
  }

  public void setSummaryOnly(boolean summaryOnly) {
    _summaryOnly = summaryOnly;
  }
}
