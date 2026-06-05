package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * One numbered entry of an SR-OS {@code policy-statement}, keyed by {@code entry-id}. Holds the
 * {@code from} match criteria modeled for P4 (prefix-list references) and the entry {@code
 * action}'s {@code action-type}.
 */
public final class PolicyStatementEntry implements Serializable {

  public PolicyStatementEntry(long entryId) {
    _entryId = entryId;
    _fromPrefixLists = new ArrayList<>();
  }

  public long getEntryId() {
    return _entryId;
  }

  /** The ordered {@code from prefix-list [...]} leaf-list (names of {@link PrefixList}s). */
  public @Nonnull List<String> getFromPrefixLists() {
    return _fromPrefixLists;
  }

  /** The entry {@code action action-type}, or {@code null} if unset. */
  public @Nullable PolicyAction getAction() {
    return _action;
  }

  public void setAction(@Nullable PolicyAction action) {
    _action = action;
  }

  private final long _entryId;
  private final @Nonnull List<String> _fromPrefixLists;
  private @Nullable PolicyAction _action;
}
