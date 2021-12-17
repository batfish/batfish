package org.batfish.datamodel.vxlan;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** A VXLAN Network Identifier configuration */
public interface Vni extends Serializable {
  /** Default UDP port on which VXLAN tunnels are established */
  Integer DEFAULT_UDP_PORT = 4789;

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
