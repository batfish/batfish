package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.Ip;

public class CryptoMapEntry implements Serializable {

  private String _accessList;

  private boolean _dynamic;

  private String _isakmpProfile;

  private Ip _peer;

  private List<String> _transforms;

  private DiffieHellmanGroup _pfsKeyGroup;

  private String _referredDynamicMapSet;

  private int _sequenceNumber;

  private final String _name;

  public CryptoMapEntry(String name, int sequenceNumber) {
    _name = name;
    _transforms = new ArrayList<>(); /* transforms or IPSec proposals are applied in order */
    _sequenceNumber = sequenceNumber;
  }

  public @Nullable String getAccessList() {
    return _accessList;
  }

  public boolean getDynamic() {
    return _dynamic;
  }

  public @Nullable String getIsakmpProfile() {
    return _isakmpProfile;
  }

  public String getName() {
    return _name;
  }

  public @Nullable Ip getPeer() {
    return _peer;
  }

  public List<String> getTransforms() {
    return _transforms;
  }

  public @Nullable DiffieHellmanGroup getPfsKeyGroup() {
    return _pfsKeyGroup;
  }

  public @Nullable String getReferredDynamicMapSet() {
    return _referredDynamicMapSet;
  }

  public int getSequenceNumber() {
    return _sequenceNumber;
  }

  public void setAccessList(String accessList) {
    _accessList = accessList;
  }

  public void setDynamic(boolean dynamic) {
    _dynamic = dynamic;
  }

  public void setIsakmpProfile(String isakmpProfile) {
    _isakmpProfile = isakmpProfile;
  }

  public void setPeer(Ip peer) {
    _peer = peer;
  }

  public void setTransforms(List<String> proposals) {
    _transforms = proposals;
  }

  public void setPfsKeyGroup(DiffieHellmanGroup pfsKeyGroup) {
    _pfsKeyGroup = pfsKeyGroup;
  }

  public void setReferredDynamicMapSet(@Nullable String referredDynamicMapSet) {
    _referredDynamicMapSet = referredDynamicMapSet;
  }

  public void setSequenceNumber(int sequenceNumber) {
    _sequenceNumber = sequenceNumber;
  }
}
