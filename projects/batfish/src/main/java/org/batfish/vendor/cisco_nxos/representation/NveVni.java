package org.batfish.vendor.cisco_nxos.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.vendor.cisco_nxos.representation.Nve.IngressReplicationProtocol;

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

  public void addPeerIp(Ip ip) {
    if (_peerIps == null) {
      _peerIps = new TreeSet<>();
    }
    _peerIps.add(ip);
  }

  public @Nonnull Set<Ip> getPeerIps() {
    return firstNonNull(_peerIps, ImmutableSet.of());
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
  private @Nullable Set<Ip> _peerIps;
  private @Nullable Boolean _suppressArp;
  private final int _vni;
}
