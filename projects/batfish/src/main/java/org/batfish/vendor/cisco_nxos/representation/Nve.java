package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/**
 * Represents an NX-OS NVE (Network Virtual Interface) [sic] - Logical interface where the
 * encapsulation and de-encapsulation occur.
 * https://www.cisco.com/c/en/us/support/docs/switches/nexus-9000-series-switches/118978-config-vxlan-00.html#anc4
 */
@ParametersAreNonnullByDefault
public final class Nve implements Serializable {
  /** "[global] ingress-replication protocol bgp|static" */
  public enum IngressReplicationProtocol {
    BGP,
    STATIC,
  }

  /** "host-reachability protocol bgp|[other options we are currently ignoring]" */
  public enum HostReachabilityProtocol {
    BGP,
  }

  public Nve(int id) {
    _id = id;
    _memberVnis = new HashMap<>();
    _shutdown = true;
  }

  public @Nullable IngressReplicationProtocol getGlobalIngressReplicationProtocol() {
    return _globalIngressReplicationProtocol;
  }

  public void setGlobalIngressReplicationProtocol(
      @Nullable IngressReplicationProtocol globalIngressReplicationProtocol) {
    _globalIngressReplicationProtocol = globalIngressReplicationProtocol;
  }

  public boolean isGlobalSuppressArp() {
    return _globalSuppressArp;
  }

  public void setGlobalSuppressArp(boolean globalSuppressArp) {
    _globalSuppressArp = globalSuppressArp;
  }

  public @Nullable HostReachabilityProtocol getHostReachabilityProtocol() {
    return _hostReachabilityProtocol;
  }

  public void setHostReachabilityProtocol(
      @Nullable HostReachabilityProtocol hostReachabilityProtocol) {
    _hostReachabilityProtocol = hostReachabilityProtocol;
  }

  public int getId() {
    return _id;
  }

  public @Nonnull NveVni getMemberVni(int vni) {
    return _memberVnis.computeIfAbsent(vni, NveVni::new);
  }

  public @Nonnull Map<Integer, NveVni> getMemberVnis() {
    return _memberVnis;
  }

  public @Nullable Ip getMulticastGroupL2() {
    return _multicastGroupL2;
  }

  public void setMulticastGroupL2(@Nullable Ip multicastGroupL2) {
    _multicastGroupL2 = multicastGroupL2;
  }

  public @Nullable Ip getMulticastGroupL3() {
    return _multicastGroupL3;
  }

  public void setMulticastGroupL3(@Nullable Ip multicastGroupL3) {
    _multicastGroupL3 = multicastGroupL3;
  }

  public boolean isShutdown() {
    return _shutdown;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }

  public @Nullable String getSourceInterface() {
    return _sourceInterface;
  }

  public void setSourceInterface(@Nullable String sourceInterface) {
    _sourceInterface = sourceInterface;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private @Nullable HostReachabilityProtocol _hostReachabilityProtocol;
  private @Nullable IngressReplicationProtocol _globalIngressReplicationProtocol;
  private boolean _globalSuppressArp;
  private final int _id;
  private @Nullable Ip _multicastGroupL2;
  private @Nullable Ip _multicastGroupL3;
  private boolean _shutdown;
  private @Nullable String _sourceInterface;
  private final Map<Integer, NveVni> _memberVnis;
}
