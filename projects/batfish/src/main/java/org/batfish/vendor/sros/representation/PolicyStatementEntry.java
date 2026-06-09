package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * One numbered entry of an SR-OS {@code policy-statement}, keyed by {@code entry-id}. Holds the
 * {@code from} match criteria modeled for P4 (prefix-list references), the entry {@code action}'s
 * {@code action-type}, and the modeled {@code action} set-clauses (metric/MED, as-path-prepend,
 * community add).
 */
public final class PolicyStatementEntry implements Serializable {

  public PolicyStatementEntry(long entryId) {
    _entryId = entryId;
    _fromPrefixLists = new ArrayList<>();
    _communityAdds = new ArrayList<>();
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

  /** The {@code action metric set <n>} value (BGP MED), or {@code null} if unset. */
  public @Nullable Long getSetMetric() {
    return _setMetric;
  }

  public void setSetMetric(@Nullable Long setMetric) {
    _setMetric = setMetric;
  }

  /** The {@code action as-path-prepend as-path <asn>} AS number, or {@code null} if unset. */
  public @Nullable Long getAsPathPrependAsn() {
    return _asPathPrependAsn;
  }

  public void setAsPathPrependAsn(@Nullable Long asPathPrependAsn) {
    _asPathPrependAsn = asPathPrependAsn;
  }

  /** The {@code action as-path-prepend repeat <n>} count; defaults to 1 when prepend is set. */
  public int getAsPathPrependRepeat() {
    return _asPathPrependRepeat;
  }

  public void setAsPathPrependRepeat(int asPathPrependRepeat) {
    _asPathPrependRepeat = asPathPrependRepeat;
  }

  /** The ordered {@code action community add [...]} community-list names (references). */
  public @Nonnull List<String> getCommunityAdds() {
    return _communityAdds;
  }

  private final long _entryId;
  private final @Nonnull List<String> _fromPrefixLists;
  private @Nullable PolicyAction _action;
  private @Nullable Long _setMetric;
  private @Nullable Long _asPathPrependAsn;
  private int _asPathPrependRepeat = 1;
  private final @Nonnull List<String> _communityAdds;
}
