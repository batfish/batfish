package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.representation.cisco_nxos.Nve.IngressReplicationProtocol;

public class NveVni implements Serializable {

  public NveVni(int vni) {
    _vni = vni;
  }

  public boolean isAssociateVrf() {
    return _associateVrf;
  }

  public void setAssociateVrf(boolean associateVrf) {
    _associateVrf = associateVrf;
  }

  public @Nullable IngressReplicationProtocol getIngressReplicationProtocol() {
    return _ingressReplicationProtocol;
  }

  public void setIngressReplicationProtocol(
      @Nullable IngressReplicationProtocol ingressReplicationProtocol) {
    _ingressReplicationProtocol = ingressReplicationProtocol;
  }

  public @Nullable Ip getMcastGroup() {
    return _mcastGroup;
  }

  public void setMcastGroup(@Nullable Ip mcastGroup) {
    _mcastGroup = mcastGroup;
  }

  public @Nullable Boolean getSuppressArp() {
    return _suppressArp;
  }

  public void setSuppressArp(@Nullable Boolean suppressArp) {
    _suppressArp = suppressArp;
  }

  public int getVni() {
    return _vni;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private boolean _associateVrf;
  private @Nullable IngressReplicationProtocol _ingressReplicationProtocol;
  private @Nullable Ip _mcastGroup;
  private @Nullable Boolean _suppressArp;
  private final int _vni;
}
