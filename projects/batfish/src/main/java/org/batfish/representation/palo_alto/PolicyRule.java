package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Objects;
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
  private @Nullable String _usedBy;
  private @Nullable PolicyRuleMatchFromPeerSet _matchFromPeerSet;
  private @Nullable PolicyRuleMatchAddressPrefixSet _matchAddressPrefixSet;

  public PolicyRule(String name) {
    _name = name;
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

  public @Nonnull Stream<PolicyRuleUpdate> getUpdates() {
    return Stream.of(_updateOrigin, _updateMetric).filter(Objects::nonNull);
  }

  public @Nullable Boolean getEnable() {
    return _enable;
  }

  public void setEnable(@Nullable Boolean enable) {
    _enable = enable;
  }

  public @Nullable String getUsedBy() {
    return _usedBy;
  }

  public void setUsedBy(@Nullable String usedBy) {
    _usedBy = usedBy;
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
