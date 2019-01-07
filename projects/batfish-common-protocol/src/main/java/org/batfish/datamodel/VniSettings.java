package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.SortedSet;

/** Settings for a specific VXLAN segment */
public final class VniSettings implements Serializable {

  private static final String PROP_BUM_TRANSPORT_IPS = "bumTransportIps";

  private static final String PROP_BUM_TRANSPORT_METHOD = "bumTransportMethod";

  private static final String PROP_SOURCE_ADDRESS = "sourceAddress";

  private static final String PROP_UDP_PORT = "udpPort";

  private static final String PROP_VLAN = "vlan";

  private static final String PROP_VNI = "vni";

  private static final long serialVersionUID = 1L;

  private final SortedSet<Ip> _bumTransportIps;

  private final BumTransportMethod _bumTransportMethod;

  private final Ip _sourceAddress;

  private final Integer _udpPort;

  private final Integer _vlan;

  private final int _vni;

  public VniSettings(
      @JsonProperty(PROP_BUM_TRANSPORT_IPS) SortedSet<Ip> bumTransportIps,
      @JsonProperty(PROP_BUM_TRANSPORT_METHOD) BumTransportMethod bumTransportMethod,
      @JsonProperty(PROP_SOURCE_ADDRESS) Ip sourceAddress,
      @JsonProperty(PROP_UDP_PORT) Integer udpPort,
      @JsonProperty(PROP_VLAN) Integer vlan,
      @JsonProperty(PROP_VNI) int vni) {
    _bumTransportIps = bumTransportIps;
    _bumTransportMethod = bumTransportMethod;
    _sourceAddress = sourceAddress;
    _udpPort = udpPort;
    _vlan = vlan;
    _vni = vni;
  }

  @JsonProperty(PROP_BUM_TRANSPORT_IPS)
  public SortedSet<Ip> getBumTransportIps() {
    return _bumTransportIps;
  }

  @JsonProperty(PROP_BUM_TRANSPORT_METHOD)
  public BumTransportMethod getBumTransportMethod() {
    return _bumTransportMethod;
  }

  @JsonProperty(PROP_SOURCE_ADDRESS)
  public Ip getSourceAddress() {
    return _sourceAddress;
  }

  @JsonProperty(PROP_UDP_PORT)
  public Integer getUdpPort() {
    return _udpPort;
  }

  @JsonProperty(PROP_VLAN)
  public Integer getVlan() {
    return _vlan;
  }

  @JsonProperty(PROP_VNI)
  public int getVni() {
    return _vni;
  }
}
