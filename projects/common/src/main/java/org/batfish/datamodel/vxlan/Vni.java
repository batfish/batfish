package org.batfish.datamodel.vxlan;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** A VXLAN Network Identifier configuration */
public interface Vni extends Serializable {
  /** Default UDP port on which VXLAN tunnels are established */
  int DEFAULT_UDP_PORT = 4789;

  /** IP address with which the encapsulated packets would be sourced */
  @Nullable
  Ip getSourceAddress();

  /** UDP port for encapsulated VXLAN traffic on this VNI. */
  int getUdpPort();

  /** VNI number */
  int getVni();

  /** Name of the VRF from which the encapsulated packets would be sourced */
  String getSrcVrf();
}
