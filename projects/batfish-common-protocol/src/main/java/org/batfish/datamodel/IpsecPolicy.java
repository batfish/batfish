package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import org.batfish.common.util.ComparableStructure;

public class IpsecPolicy extends ComparableStructure<String> {

  private static final String PROP_PROPOSALS = "proposals";

  /** */
  private static final long serialVersionUID = 1L;

  private IkeGateway _ikeGateway;

  private DiffieHellmanGroup _pfsKeyGroup;

  private boolean _pfsKeyGroupDynamicIke;

  private transient SortedSet<String> _proposalNames;

  private List<IpsecProposal> _proposals;

  @JsonCreator
  public IpsecPolicy(@JsonProperty(PROP_NAME) String name) {
    super(name);
    _proposals =
        new ArrayList<>(); /* IPSec proposals are applied in the order they are specified */
  }

  public DiffieHellmanGroup getPfsKeyGroup() {
    return _pfsKeyGroup;
  }

  public boolean getPfsKeyGroupDynamicIke() {
    return _pfsKeyGroupDynamicIke;
  }

  public IkeGateway getIkeGateway() {
    return _ikeGateway;
  }

  @JsonProperty(PROP_PROPOSALS)
  @JsonPropertyDescription("IPSEC proposals to try with this policy")
  public SortedSet<String> getProposalNames() {
    if (_proposals != null && !_proposals.isEmpty()) {
      return _proposals
          .stream()
          .map(IpsecProposal::getName)
          .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
    } else {
      return _proposalNames;
    }
  }

  @JsonIgnore
  public List<IpsecProposal> getProposals() {
    return _proposals;
  }

  public void resolveReferences(Configuration owner) {
    if (_proposalNames != null) {
      for (String name : _proposalNames) {
        _proposals.add(owner.getIpsecProposals().get(name));
      }
    }
  }

  public void setIkeGateway(IkeGateway ikeGateway) {
    _ikeGateway = ikeGateway;
  }

  public void setPfsKeyGroup(DiffieHellmanGroup dhGroup) {
    _pfsKeyGroup = dhGroup;
  }

  public void setPfsKeyGroupDynamicIke(boolean pfsKeyGroupDynamicIke) {
    _pfsKeyGroupDynamicIke = pfsKeyGroupDynamicIke;
  }

  @JsonProperty(PROP_PROPOSALS)
  public void setProposalNames(SortedSet<String> proposalNames) {
    _proposalNames = proposalNames;
  }

  public void setProposals(List<IpsecProposal> proposals) {
    _proposals = proposals;
  }
}
