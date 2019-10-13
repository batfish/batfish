package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A rule in the collection of import or export policy rules */
public final class PolicyRule implements Serializable {

  private @Nonnull String _name;
  private @Nullable PolicyRuleUpdateOrigin _updateOrigin;
  private @Nullable PolicyRuleUpdateMetric _updateMetric;
  private @Nullable PolicyRuleMatchFromPeerSet _matchFromPeerSet;
  private @Nullable PolicyRuleMatchAddressPrefixSet _matchAddressPrefixSet;

  public PolicyRule(String name) {
    _name = name;
  }

  @Nonnull
  public String getName() {
    return _name;
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

  public void setUpdateOrigin(@Nullable PolicyRuleUpdateOrigin updateOrigin) {
    _updateOrigin = updateOrigin;
  }

  public void setUpdateMetric(@Nullable PolicyRuleUpdateMetric updateMetric) {
    _updateMetric = updateMetric;
  }

  @Nullable
  public PolicyRuleMatchFromPeerSet getMatchFromPeerSet() {
    return _matchFromPeerSet;
  }

  public void setMatchFromPeerSet(@Nullable PolicyRuleMatchFromPeerSet matchFromPeerSet) {
    _matchFromPeerSet = matchFromPeerSet;
  }

  @Nullable
  public PolicyRuleMatchAddressPrefixSet getMatchAddressPrefixSet() {
    return _matchAddressPrefixSet;
  }

  public void setMatchAddressPrefixSet(
      @Nullable PolicyRuleMatchAddressPrefixSet matchAddressPrefixSet) {
    _matchAddressPrefixSet = matchAddressPrefixSet;
  }
}
