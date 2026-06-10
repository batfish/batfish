package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.OriginType;

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
    _fromProtocols = new ArrayList<>();
    _fromCommunities = new ArrayList<>();
    _fromAsPaths = new ArrayList<>();
    _communityAdds = new ArrayList<>();
  }

  public long getEntryId() {
    return _entryId;
  }

  /** The ordered {@code from prefix-list [...]} leaf-list (names of {@link PrefixList}s). */
  public @Nonnull List<String> getFromPrefixLists() {
    return _fromPrefixLists;
  }

  /**
   * The {@code from protocol name [...]} leaf-list — the protocols the route must have been learned
   * from to match. Unrecognized protocol names are warned and dropped at extraction.
   */
  public @Nonnull List<FromProtocol> getFromProtocols() {
    return _fromProtocols;
  }

  /** The {@code from community name <name>} community-list names the route must match (ANDed). */
  public @Nonnull List<String> getFromCommunities() {
    return _fromCommunities;
  }

  /** The {@code from as-path name <name>} as-path-list names the route's AS path must match. */
  public @Nonnull List<String> getFromAsPaths() {
    return _fromAsPaths;
  }

  /** The entry {@code action action-type}, or {@code null} if unset. */
  public @Nullable PolicyAction getAction() {
    return _action;
  }

  /** The {@code action local-preference <n>} value (BGP local-pref), or {@code null} if unset. */
  public @Nullable Long getSetLocalPreference() {
    return _setLocalPreference;
  }

  public void setSetLocalPreference(@Nullable Long setLocalPreference) {
    _setLocalPreference = setLocalPreference;
  }

  /** The {@code action origin <igp|egp|incomplete>} value, or {@code null} if unset. */
  public @Nullable OriginType getSetOrigin() {
    return _setOrigin;
  }

  public void setSetOrigin(@Nullable OriginType setOrigin) {
    _setOrigin = setOrigin;
  }

  /** The {@code action metric add <n>} value (added to the route's metric), or {@code null}. */
  public @Nullable Long getMetricAdd() {
    return _metricAdd;
  }

  public void setMetricAdd(@Nullable Long metricAdd) {
    _metricAdd = metricAdd;
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
  private final @Nonnull List<FromProtocol> _fromProtocols;
  private final @Nonnull List<String> _fromCommunities;
  private final @Nonnull List<String> _fromAsPaths;
  private @Nullable PolicyAction _action;
  private @Nullable Long _setMetric;
  private @Nullable Long _setLocalPreference;
  private @Nullable OriginType _setOrigin;
  private @Nullable Long _metricAdd;
  private @Nullable Long _asPathPrependAsn;
  private int _asPathPrependRepeat = 1;
  private final @Nonnull List<String> _communityAdds;
}
