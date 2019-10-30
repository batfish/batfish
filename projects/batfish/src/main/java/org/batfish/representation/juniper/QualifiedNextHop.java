package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a qualified next-hop configured for a {@link StaticRoute} */
public class QualifiedNextHop implements Serializable {
  @Nullable private Integer _metric;
  @Nonnull private NextHop _nextHop;
  @Nullable private Integer _preference;
  @Nullable private Long _tag;

  public QualifiedNextHop(@Nonnull NextHop nextHop) {
    _nextHop = nextHop;
  }

  @Nullable
  public Integer getMetric() {
    return _metric;
  }

  public void setMetric(@Nullable Integer metric) {
    _metric = metric;
  }

  @Nullable
  public Integer getPreference() {
    return _preference;
  }

  public void setPreference(@Nullable Integer preference) {
    _preference = preference;
  }

  @Nullable
  public Long getTag() {
    return _tag;
  }

  public void setTag(@Nullable Long tag) {
    _tag = tag;
  }

  @Nonnull
  public NextHop getNextHop() {
    return _nextHop;
  }
}
