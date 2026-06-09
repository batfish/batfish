package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An SR-OS {@code policy-options policy-statement}, keyed by name. Holds its numbered {@link
 * PolicyStatementEntry entries} (ordered by entry-id) and the {@code default-action action-type}
 * applied when no entry matches.
 */
public final class PolicyStatement implements Serializable {

  public PolicyStatement(String name) {
    _name = name;
    _entries = new LinkedHashMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** Numbered entries, keyed by entry-id. A {@link LinkedHashMap} preserves configuration order. */
  public @Nonnull Map<Long, PolicyStatementEntry> getEntries() {
    return _entries;
  }

  /** The {@code default-action action-type}, or {@code null} if unset. */
  public @Nullable PolicyAction getDefaultAction() {
    return _defaultAction;
  }

  public void setDefaultAction(@Nullable PolicyAction defaultAction) {
    _defaultAction = defaultAction;
  }

  private final @Nonnull String _name;
  private final @Nonnull Map<Long, PolicyStatementEntry> _entries;
  private @Nullable PolicyAction _defaultAction;
}
