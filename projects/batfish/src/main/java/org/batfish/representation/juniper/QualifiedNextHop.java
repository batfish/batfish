package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a qualified next-hop configured for a {@link StaticRoute} */
public class QualifiedNextHop implements Serializable {
  private @Nullable Integer _metric;
  private @Nonnull NextHop _nextHop;
  private @Nullable Integer _preference;
  private @Nullable Long _tag;

  public QualifiedNextHop(@Nonnull NextHop nextHop) {
    _nextHop = nextHop;
  }

  public @Nullable Integer getMetric() {
    return _metric;
  }

  public void setMetric(@Nullable Integer metric) {
    _metric = metric;
  }

  public @Nullable Integer getPreference() {
    return _preference;
  }

  public void setPreference(@Nullable Integer preference) {
    _preference = preference;
  }

  public @Nullable Long getTag() {
    return _tag;
  }

  public void setTag(@Nullable Long tag) {
    _tag = tag;
  }

  public @Nonnull NextHop getNextHop() {
    return _nextHop;
  }
}
