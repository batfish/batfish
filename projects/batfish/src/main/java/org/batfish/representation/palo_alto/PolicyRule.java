package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A rule in the collection of import or export policy rules */
public final class PolicyRule implements Serializable {
  public enum Action {
    ALLOW,
    DENY
  }

  private final @Nonnull String _name;
  private @Nullable Action _action;
  private @Nullable Boolean _enable;
  private @Nullable PolicyRuleUpdateOrigin _updateOrigin;
  private @Nullable PolicyRuleUpdateMetric _updateMetric;
  private @Nullable PolicyRuleUpdateWeight _updateWeight;
  private @Nonnull Set<String> _usedBy;
  private @Nullable PolicyRuleMatchFromPeerSet _matchFromPeerSet;
  private @Nullable PolicyRuleMatchAddressPrefixSet _matchAddressPrefixSet;

  public PolicyRule(String name) {
    _name = name;
    _usedBy = new HashSet<>(1);
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Action getAction() {
    return _action;
  }

  public void setAction(@Nullable Action action) {
    _action = action;
  }

  public @Nullable PolicyRuleUpdateOrigin getUpdateOrigin() {
    return _updateOrigin;
  }

  public @Nullable PolicyRuleUpdateMetric getUpdateMetric() {
    return _updateMetric;
  }

  @Nullable
  public PolicyRuleUpdateWeight getUpdateWeight() {
    return _updateWeight;
  }

  public void setUpdateWeight(@Nullable PolicyRuleUpdateWeight updateWeight) {
    _updateWeight = updateWeight;
  }

  public @Nonnull Stream<PolicyRuleUpdate> getUpdates() {
    return Stream.of(_updateOrigin, _updateMetric, _updateWeight).filter(Objects::nonNull);
  }

  public @Nonnull Stream<PolicyRuleMatch> getMatches() {
    return Stream.of(_matchAddressPrefixSet, _matchFromPeerSet).filter(Objects::nonNull);
  }

  public @Nullable Boolean getEnable() {
    return _enable;
  }

  public void setEnable(@Nullable Boolean enable) {
    _enable = enable;
  }

  public @Nonnull Set<String> getUsedBy() {
    return _usedBy;
  }

  public void addUsedBy(@Nonnull String usedBy) {
    _usedBy.add(usedBy);
  }

  public void setUpdateOrigin(@Nullable PolicyRuleUpdateOrigin updateOrigin) {
    _updateOrigin = updateOrigin;
  }

  public void setUpdateMetric(@Nullable PolicyRuleUpdateMetric updateMetric) {
    _updateMetric = updateMetric;
  }

  public @Nullable PolicyRuleMatchFromPeerSet getMatchFromPeerSet() {
    return _matchFromPeerSet;
  }

  public void setMatchFromPeerSet(@Nullable PolicyRuleMatchFromPeerSet matchFromPeerSet) {
    _matchFromPeerSet = matchFromPeerSet;
  }

  public @Nonnull PolicyRuleMatchAddressPrefixSet getOrCreateMatchAddressPrefixSet() {
    if (_matchAddressPrefixSet == null) {
      _matchAddressPrefixSet = new PolicyRuleMatchAddressPrefixSet();
    }
    return _matchAddressPrefixSet;
  }

  public @Nullable PolicyRuleMatchAddressPrefixSet getMatchAddressPrefixSet() {
    return _matchAddressPrefixSet;
  }
}
