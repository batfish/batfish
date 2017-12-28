package org.batfish.representation.aws;

import java.io.Serializable;
import org.batfish.datamodel.Ip;

public class NatGatewayAddress implements Serializable {

  private static final long serialVersionUID = 1L;

  public final String _allocationId;
  public final String _networkInterfaceId;
  public final Ip _privateIp;
  public final Ip _publicIp;

  public NatGatewayAddress(
      String allocationId, String networkInterfaceId, Ip privateIp, Ip publicIp) {
    _allocationId = allocationId;
    _networkInterfaceId = networkInterfaceId;
    _privateIp = privateIp;
    _publicIp = publicIp;
  }
}
