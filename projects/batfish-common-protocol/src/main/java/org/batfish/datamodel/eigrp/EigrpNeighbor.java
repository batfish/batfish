package org.batfish.datamodel.eigrp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;

/**
 * Representation of EIGRP router as one half of a {@link
 * org.batfish.datamodel.collections.VerboseEigrpEdge}
 */
public class EigrpNeighbor extends ComparableStructure<IpLink> {

  private static final long serialVersionUID = 1L;
  private long _asn;
  private Interface _iface;
  private Configuration _owner;
  private transient EigrpNeighbor _remoteEigrpNeighbor;
  private String _vrf;

  @JsonCreator
  public EigrpNeighbor(@JsonProperty(PROP_NAME) IpLink ipEdge) {
    super(ipEdge);
  }

  public long getAsn() {
    return _asn;
  }

  public void setAsn(long asn) {
    _asn = asn;
  }

  public Interface getIface() {
    return _iface;
  }

  public void setIface(Interface iface) {
    _iface = iface;
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

  public void setOwner(Configuration owner) {
    _owner = owner;
  }

  @JsonIgnore
  public Ip getRemoteIp() {
    return _key.getIp2();
  }

  @JsonIgnore
  public EigrpNeighbor getRemoteEigrpNeigbor() {
    return _remoteEigrpNeighbor;
  }

  public String getVrf() {
    return _vrf;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }

  public void setInterface(Interface iface) {
    _iface = iface;
  }

  public void setRemoteEigrpNeighbor(EigrpNeighbor remoteEigrpNeighbor) {
    _remoteEigrpNeighbor = remoteEigrpNeighbor;
  }

  public static final class EigrpNeighborSummary extends ComparableStructure<String> {

    private static final String PROP_LOCAL_IP = "localIp";

    private static final String PROP_REMOTE_IP = "remoteIp";

    private static final long serialVersionUID = 1L;

    private static final String PROP_VRF = "vrf";

    private final Ip _localIp;

    private final Ip _remoteIp;

    private final String _vrf;

    public EigrpNeighborSummary(EigrpNeighbor eigrpNeighbor) {
      super(eigrpNeighbor.getOwner().getName() + ":" + eigrpNeighbor._key);
      _localIp = eigrpNeighbor._key.getIp1();
      _remoteIp = eigrpNeighbor._key.getIp2();
      _vrf = eigrpNeighbor._vrf;
    }

    @JsonCreator
    public EigrpNeighborSummary(
        @JsonProperty(PROP_NAME) String name,
        @JsonProperty(PROP_LOCAL_IP) Ip localIp,
        @JsonProperty(PROP_REMOTE_IP) Ip remoteIp,
        @JsonProperty(PROP_VRF) String vrf) {
      super(vrf);
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
}
