package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IpsecPhase2Policy implements Serializable {
  private static final String PROP_PFS_KEY_GROUP = "pfsKeyGroup";
  private static final String PROP_PROPOSALS = "proposals";

  private @Nonnull Set<DiffieHellmanGroup> _pfsKeyGroups;

  private @Nonnull List<String> _proposals;

  @JsonCreator
  public IpsecPhase2Policy() {
    _proposals = ImmutableList.of();
    _pfsKeyGroups = ImmutableSet.of();
  }

  /** Diffie Hellman groups to be used for Perfect Forward Secrecy. */
  public Set<DiffieHellmanGroup> getPfsKeyGroups() {
    return _pfsKeyGroups;
  }

  /**
   * Returns a sorted set of PFS key groups for serialization. This ensures deterministic output for
   * reference tests.
   */
  @JsonProperty(PROP_PFS_KEY_GROUP)
  private Set<DiffieHellmanGroup> getSortedPfsKeyGroups() {
    return ImmutableSet.copyOf(
        _pfsKeyGroups.stream()
            .sorted(Comparator.comparingInt(DiffieHellmanGroup::getGroupNumber))
            .collect(ImmutableSet.toImmutableSet()));
  }

  /** IPSec phase 1 proposals to be used with this IPSec policy. */
  @JsonProperty(PROP_PROPOSALS)
  public List<String> getProposals() {
    return _proposals;
  }

  /**
   * Sets the PFS key groups.
   *
   * @param dhGroups Set of Diffie Hellman groups
   */
  @JsonProperty(PROP_PFS_KEY_GROUP)
  public void setPfsKeyGroups(@Nullable Set<DiffieHellmanGroup> dhGroups) {
    _pfsKeyGroups = dhGroups == null ? ImmutableSet.of() : ImmutableSet.copyOf(dhGroups);
  }

  /**
   * Sets a single PFS key group. This method is for backward compatibility.
   *
   * @param dhGroup Diffie Hellman group
   */
  public void setPfsKeyGroup(@Nullable DiffieHellmanGroup dhGroup) {
    _pfsKeyGroups = dhGroup == null ? ImmutableSet.of() : ImmutableSet.of(dhGroup);
  }

  @JsonProperty(PROP_PROPOSALS)
  public void setProposals(@Nullable List<String> proposals) {
    _proposals = proposals == null ? ImmutableList.of() : ImmutableList.copyOf(proposals);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof IpsecPhase2Policy)) {
      return false;
    }
    IpsecPhase2Policy that = (IpsecPhase2Policy) o;
    return _pfsKeyGroups.equals(that._pfsKeyGroups) && _proposals.equals(that._proposals);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_pfsKeyGroups, _proposals);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(IpsecPhase2Policy.class)
        .add("pfsKeyGroups", _pfsKeyGroups)
        .add("proposals", _proposals)
        .toString();
  }
}
