package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An SR-OS {@code policy-options as-path "<name>"} entry, keyed by name. Holds the {@code
 * expression} — a regex over the AS path (e.g. {@code "65003"} matches a path containing AS 65003).
 * A policy {@code from as-path name "<name>"} references this by name.
 */
public final class AsPathList implements Serializable {

  public AsPathList(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** The {@code expression} regex over the AS path, or {@code null} if unset. */
  public @Nullable String getExpression() {
    return _expression;
  }

  public void setExpression(@Nullable String expression) {
    _expression = expression;
  }

  private final @Nonnull String _name;
  private @Nullable String _expression;
}
