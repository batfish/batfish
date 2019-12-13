package org.batfish.datamodel.vxlan;

import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Ip;

/** A VXLAN Network Identifier configuration */
public interface Vni extends Serializable {
  /** Default UDP port on which VXLAN tunnels are established */
  Integer DEFAULT_UDP_PORT = 4789;

  @Nullable
  Ip getMulticastGroup();

  @Nonnull
  Set<Ip> getBumTransportIps();

  @Nonnull
  BumTransportMethod getBumTransportMethod();

  /** IP address with which the encapsulated packets would be sourced */
  @Nullable
  Ip getSourceAddress();

  @Nonnull
  Integer getUdpPort();

  /** VNI number */
  int getVni();

  /** Name of the VRF from which the encapsulated packets would be sourced */
  String getSrcVrf();
}
