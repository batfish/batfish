package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

public class StaticRoute implements Serializable {

  /* https://www.paloaltonetworks.com/documentation/80/pan-os/pan-os/networking/static-routes/static-route-overview */
  private static final int DEFAULT_ADMIN_DISTANCE = 10;

  /* Static routes show up with default metric of 10 when showing routes on PAN device */
  private static final int DEFAULT_METRIC = 10;

  private int _adminDistance;
  private Prefix _destination;
  private int _metric;
  private final String _name;
  private boolean _nextHopDiscard;
  private @Nullable String _nextHopInterface;
  private @Nullable InterfaceAddress _nextHopIp;
  private @Nullable String _nextVr;

  public StaticRoute(String name) {
    _name = name;
    _adminDistance = DEFAULT_ADMIN_DISTANCE;
    _metric = DEFAULT_METRIC;
  }

  public int getAdminDistance() {
    return _adminDistance;
  }

  public Prefix getDestination() {
    return _destination;
  }

  public int getMetric() {
    return _metric;
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getNextHopInterface() {
    return _nextHopInterface;
  }

  public @Nullable InterfaceAddress getNextHopIp() {
    return _nextHopIp;
  }

  public @Nullable String getNextVr() {
    return _nextVr;
  }

  public void setAdminDistance(int adminDistance) {
    _adminDistance = adminDistance;
  }

  public void setDestination(Prefix destination) {
    _destination = destination;
  }

  public void setMetric(int metric) {
    _metric = metric;
  }

  public boolean getNextHopDiscard() {
    return _nextHopDiscard;
  }

  public void setNextHopDiscard() {
    _nextHopDiscard = true;
    // TODO: will this clear or warn?
    _nextHopInterface = null;
    _nextHopIp = null;
    _nextVr = null;
  }

  public void setNextHopInterface(String nextHopInterface) {
    _nextHopInterface = nextHopInterface;
    // keep nexthop ip-address
    // TODO: will this clear or warn?
    _nextHopDiscard = false;
    _nextVr = null;
  }

  public void setNextHopIp(InterfaceAddress nextHopIp) {
    _nextHopIp = nextHopIp;
    // keep nexthop interface
    // TODO: will this clear or warn?
    _nextHopDiscard = false;
    _nextVr = null;
  }

  public void setNextVr(@Nullable String nextVr) {
    _nextVr = nextVr;
    // TODO: will this clear or warn?
    _nextHopDiscard = false;
    _nextHopIp = null;
    _nextHopInterface = null;
  }
}
