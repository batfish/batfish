package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A profile used for redistribution under different protocols */
public final class RedistProfile implements Serializable {
  public enum Action {
    NO_REDIST,
    REDIST
  }

  public RedistProfile(String name) {
    _name = name;
  }

  @Nullable
  public Action getAction() {
    return _action;
  }

  public void setAction(@Nullable Action action) {
    _action = action;
  }

  @Nonnull
  public RedistProfileFilter getOrCreateFilter() {
    if (_filter == null) {
      _filter = new RedistProfileFilter();
    }
    return _filter;
  }

  @Nullable
  public RedistProfileFilter getFilter() {
    return _filter;
  }

  public void setFilter(@Nullable RedistProfileFilter filter) {
    _filter = filter;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public Integer getPriority() {
    return _priority;
  }

  public void setPriority(@Nullable Integer priority) {
    _priority = priority;
  }

  private @Nullable Action _action;
  private @Nullable RedistProfileFilter _filter;
  private final @Nonnull String _name;
  private @Nullable Integer _priority;
}
