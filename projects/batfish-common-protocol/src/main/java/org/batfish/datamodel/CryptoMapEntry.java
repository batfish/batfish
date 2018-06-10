package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a crypto map entry which groups together to form a {@link CryptoMapSet} */
@JsonSchemaDescription("An entry in the CryptoMapSet")
public class CryptoMapEntry implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final String PROP_ACCESS_LIST = "accessList";

  private static final String PROP_DYNAMIC = "dynamic";

  private static final String PROP_IKE_GATEWAY = "ikeGateway";

  private static final String PROP_NAME = "name";

  private static final String PROP_PEER = "peer";

  private static final String PROP_PROPOSALS_ = "proposals";

  private static final String PROP_PFS_KEY_GROUP_ = "pfsKeyGroup";

  private static final String PROP_REFERRED_DYNAMIC_MAP_SET = "referredDynamicMapSet";

  private static final String PROP_SEQUENCE_NUMBER = "sequenceNumber";

  private IpAccessList _accessList;

  private boolean _dynamic;

  private IkeGateway _ikeGateway;

  private String _name;

  private Ip _peer;

  private List<IpsecProposal> _proposals;

  private DiffieHellmanGroup _pfsKeyGroup;

  private String _referredDynamicMapSet;

  private int _sequenceNumber;

  @JsonCreator
  public CryptoMapEntry(
      @JsonProperty(PROP_NAME) @Nonnull String name,
      @JsonProperty(PROP_SEQUENCE_NUMBER) int sequenceNumber) {
    _name = name;
    _proposals = new ArrayList<>(); /* transforms or Ipsec proposals are applied in order */
    _sequenceNumber = sequenceNumber;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CryptoMapEntry)) {
      return false;
    }
    CryptoMapEntry rhs = (CryptoMapEntry) obj;
    return Objects.equals(_accessList, rhs._accessList)
        && Objects.equals(_dynamic, rhs._dynamic)
        && Objects.equals(_ikeGateway, rhs._ikeGateway)
        && Objects.equals(_name, rhs._name)
        && Objects.equals(_peer, rhs._peer)
        && Objects.equals(_proposals, rhs._proposals)
        && Objects.equals(_pfsKeyGroup, rhs._pfsKeyGroup)
        && Objects.equals(_referredDynamicMapSet, rhs._referredDynamicMapSet)
        && Objects.equals(_sequenceNumber, rhs._sequenceNumber);
  }

  @JsonProperty(PROP_ACCESS_LIST)
  public @Nullable IpAccessList getAccessList() {
    return _accessList;
  }

  @JsonProperty(PROP_DYNAMIC)
  public boolean getDynamic() {
    return _dynamic;
  }

  @JsonProperty(PROP_IKE_GATEWAY)
  public @Nullable IkeGateway getIkeGateway() {
    return _ikeGateway;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_PEER)
  public @Nullable Ip getPeer() {
    return _peer;
  }

  @JsonProperty(PROP_PROPOSALS_)
  public List<IpsecProposal> getProposals() {
    return _proposals;
  }

  @JsonProperty(PROP_PFS_KEY_GROUP_)
  public @Nullable DiffieHellmanGroup getPfsKeyGroup() {
    return _pfsKeyGroup;
  }

  @JsonProperty(PROP_REFERRED_DYNAMIC_MAP_SET)
  public @Nullable String getReferredDynamicMapSet() {
    return _referredDynamicMapSet;
  }

  @JsonProperty(PROP_SEQUENCE_NUMBER)
  public int getSequenceNumber() {
    return _sequenceNumber;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _accessList,
        _dynamic,
        _ikeGateway,
        _name,
        _peer,
        _proposals,
        _pfsKeyGroup,
        _referredDynamicMapSet,
        _sequenceNumber);
  }

  @JsonProperty(PROP_ACCESS_LIST)
  public void setAccessList(IpAccessList accessList) {
    _accessList = accessList;
  }

  @JsonProperty(PROP_DYNAMIC)
  public void setDynamic(boolean dynamic) {
    _dynamic = dynamic;
  }

  @JsonProperty(PROP_IKE_GATEWAY)
  public void setIkeGateway(IkeGateway ikeGateway) {
    _ikeGateway = ikeGateway;
  }

  @JsonProperty(PROP_NAME)
  public void setName(String name) {
    _name = name;
  }

  @JsonProperty(PROP_PEER)
  public void setPeer(Ip peer) {
    _peer = peer;
  }

  @JsonProperty(PROP_PROPOSALS_)
  public void setProposals(List<IpsecProposal> proposals) {
    _proposals = proposals;
  }

  @JsonProperty(PROP_PFS_KEY_GROUP_)
  public void setPfsKeyGroup(DiffieHellmanGroup pfsKeyGroup) {
    _pfsKeyGroup = pfsKeyGroup;
  }

  @JsonProperty(PROP_REFERRED_DYNAMIC_MAP_SET)
  public void setReferredDynamicMapSet(@Nullable String referredDynamicMapSet) {
    _referredDynamicMapSet = referredDynamicMapSet;
  }

  @JsonProperty(PROP_SEQUENCE_NUMBER)
  public void setSequenceNumber(int sequenceNumber) {
    _sequenceNumber = sequenceNumber;
  }
}
