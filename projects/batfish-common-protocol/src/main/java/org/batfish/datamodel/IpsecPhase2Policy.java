package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IpsecPhase2Policy implements Serializable {
  private static final String PROP_PFS_KEY_GROUP = "pfsKeyGroup";
  private static final String PROP_PROPOSALS = "proposals";

  private DiffieHellmanGroup _pfsKeyGroup;

  private @Nonnull List<String> _proposals;

  @JsonCreator
  public IpsecPhase2Policy() {
    _proposals = ImmutableList.of();
  }

  /** Diffie Hellman group to be used for Perfect Forward Secrecy. */
  @JsonProperty(PROP_PFS_KEY_GROUP)
  public DiffieHellmanGroup getPfsKeyGroup() {
    return _pfsKeyGroup;
  }

  /** IPSec phase 1 proposals to be used with this IPSec policy. */
  @JsonProperty(PROP_PROPOSALS)
  public List<String> getProposals() {
    return _proposals;
  }

  @JsonProperty(PROP_PFS_KEY_GROUP)
  public void setPfsKeyGroup(@Nullable DiffieHellmanGroup dhGroup) {
    _pfsKeyGroup = dhGroup;
  }

  @JsonProperty(PROP_PROPOSALS)
  public void setProposals(@Nullable List<String> proposals) {
    _proposals = proposals == null ? ImmutableList.of() : ImmutableList.copyOf(proposals);
  }
}
