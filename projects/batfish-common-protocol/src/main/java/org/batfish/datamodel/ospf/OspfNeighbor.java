package org.batfish.datamodel.ospf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;

public class OspfNeighbor extends ComparableStructure<IpLink> {

  private static final String PROP_AREA = "area";

  private static final String PROP_IFACE = "iface";

  private static final String PROP_VRF = "vrf";

  public static final class OspfNeighborSummary extends ComparableStructure<String> {

    private static final String PROP_LOCAL_IP = "localIp";

    private static final String PROP_REMOTE_IP = "remoteIp";

    /** */
    private static final long serialVersionUID = 1L;

    private static final String PROP_VRF = "vrf";

    private final Ip _localIp;

    private final Ip _remoteIp;

    private final String _vrf;

    public OspfNeighborSummary(OspfNeighbor ospfNeighbor) {
      super(ospfNeighbor.getOwner().getHostname() + ":" + ospfNeighbor._key);
      _localIp = ospfNeighbor._key.getIp1();
      _remoteIp = ospfNeighbor._key.getIp2();
      _vrf = ospfNeighbor._vrf;
    }

    @JsonCreator
    public OspfNeighborSummary(
        @JsonProperty(PROP_NAME) String name,
        @JsonProperty(PROP_LOCAL_IP) Ip localIp,
        @JsonProperty(PROP_REMOTE_IP) Ip remoteIp,
        @JsonProperty(PROP_VRF) String vrf) {
      super(name);
      _localIp = localIp;
      _remoteIp = remoteIp;
      _vrf = vrf;
    }

    @JsonProperty(PROP_LOCAL_IP)
    public Ip getLocalIp() {
      return _localIp;
    }

    @JsonProperty(PROP_REMOTE_IP)
    public Ip getRemoteIp() {
      return _remoteIp;
    }

    @JsonProperty(PROP_VRF)
    public String getVrf() {
      return _vrf;
    }
  }

  /** */
  private static final long serialVersionUID = 1L;

  private long _area;

  private Interface _iface;

  private Configuration _owner;

  private transient OspfNeighbor _remoteOspfNeighbor;

  private String _vrf;

  @JsonCreator
  public OspfNeighbor(@JsonProperty(PROP_NAME) IpLink ipEdge) {
    super(ipEdge);
  }

  @JsonProperty(PROP_AREA)
  public long getArea() {
    return _area;
  }

  @JsonProperty(PROP_IFACE)
  public Interface getIface() {
    return _iface;
  }

  @JsonIgnore
  public IpLink getIpLink() {
    return _key;
  }

  @JsonIgnore
  public Ip getLocalIp() {
    return _key.getIp1();
  }

  @JsonIgnore
  public Configuration getOwner() {
    return _owner;
  }

  @JsonIgnore
  public Ip getRemoteIp() {
    return _key.getIp2();
  }

  @JsonIgnore
  public OspfNeighbor getRemoteOspfNeighbor() {
    return _remoteOspfNeighbor;
  }

  @JsonProperty(PROP_VRF)
  public String getVrf() {
    return _vrf;
  }

  @JsonProperty(PROP_AREA)
  public void setArea(long area) {
    _area = area;
  }

  @JsonProperty(PROP_IFACE)
  public void setIface(Interface iface) {
    _iface = iface;
  }

  public void setInterface(Interface iface) {
    _iface = iface;
  }

  public void setOwner(Configuration owner) {
    _owner = owner;
  }

  public void setRemoteOspfNeighbor(OspfNeighbor remoteOspfNeighbor) {
    _remoteOspfNeighbor = remoteOspfNeighbor;
  }

  @JsonProperty(PROP_VRF)
  public void setVrf(String vrf) {
    _vrf = vrf;
  }
}
