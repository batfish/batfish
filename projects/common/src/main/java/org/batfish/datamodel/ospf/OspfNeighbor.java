package org.batfish.datamodel.ospf;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpLink;

/** Configuration for one end of an OSPF adjacency */
@ParametersAreNonnullByDefault
public final class OspfNeighbor implements Comparable<OspfNeighbor> {
  private static final String PROP_AREA = "area";
  private static final String PROP_IFACE = "iface";
  private static final String PROP_NAME = "name";
  private static final String PROP_VRF = "vrf";

  // TODO: don't store full Config/Interface objects, but identifiers instead.
  private long _area;
  private Interface _iface;
  private final IpLink _link;
  private Configuration _owner;
  private transient OspfNeighbor _remoteOspfNeighbor;
  private String _vrf;

  @JsonCreator
  private static OspfNeighbor create(@JsonProperty(PROP_NAME) @Nullable IpLink ipEdge) {
    checkArgument(ipEdge != null);
    return new OspfNeighbor(ipEdge);
  }

  public OspfNeighbor(IpLink ipLink) {
    _link = ipLink;
  }

  @JsonProperty(PROP_AREA)
  public long getArea() {
    return _area;
  }

  @JsonProperty(PROP_IFACE)
  public Interface getIface() {
    return _iface;
  }

  @JsonProperty(PROP_NAME)
  public IpLink getIpLink() {
    return _link;
  }

  @JsonIgnore
  public Ip getLocalIp() {
    return _link.getIp1();
  }

  @JsonIgnore
  public Configuration getOwner() {
    return _owner;
  }

  @JsonIgnore
  public Ip getRemoteIp() {
    return _link.getIp2();
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

  @Deprecated
  public void setIface(Interface iface) {
    setInterface(iface);
  }

  @JsonProperty(PROP_IFACE)
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OspfNeighbor that = (OspfNeighbor) o;
    return _area == that._area
        && Objects.equals(_iface, that._iface)
        && Objects.equals(_link, that._link)
        && Objects.equals(_vrf, that._vrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_area, _iface, _link, _vrf);
  }

  @Override
  public int compareTo(OspfNeighbor other) {
    return Comparator.comparing(OspfNeighbor::getIpLink).compare(this, other);
  }
}
