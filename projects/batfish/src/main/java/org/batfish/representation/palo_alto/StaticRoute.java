package org.batfish.representation.palo_alto;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public class StaticRoute extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  /* https://www.paloaltonetworks.com/documentation/80/pan-os/pan-os/networking/static-routes/static-route-overview */
  private static final int DEFAULT_ADMIN_DISTANCE = 10;

  /* Static routes show up with default metric of 10 when showing routes on PAN device */
  private static final int DEFAULT_METRIC = 10;

  private int _adminDistance;

  private boolean _discard;

  private int _metric;

  private String _nextHopInterface;

  private Ip _nextHopIp;

  private Prefix _destination;

  private Integer _tag;

  public StaticRoute(String name) {
    super(name);
    // default admin costs for static routes in PAN
    _adminDistance = DEFAULT_ADMIN_DISTANCE;
    _metric = DEFAULT_METRIC;
  }

  public Prefix getDestination() {
    return _destination;
  }

  public int getAdminDistance() {
    return _adminDistance;
  }

  public boolean getDiscard() {
    return _discard;
  }

  public int getMetric() {
    return _metric;
  }

  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  public Integer getTag() {
    return _tag;
  }

  public void setAdminDistance(int adminDistance) {
    _adminDistance = adminDistance;
  }

  public void setDestination(Prefix destination) {
    _destination = destination;
  }

  public void setDiscard(boolean discard) {
    _discard = discard;
  }

  public void setMetric(int metric) {
    _metric = metric;
  }

  public void setNextHopInterface(String nextHopInterface) {
    _nextHopInterface = nextHopInterface;
  }

  public void setNextHopIp(Ip nextHopIp) {
    _nextHopIp = nextHopIp;
  }

  public void setTag(int tag) {
    _tag = tag;
  }
}
