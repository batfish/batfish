package org.batfish.representation.vyos;

import java.io.Serializable;
import org.batfish.datamodel.Ip;

public class BgpNeighbor implements Serializable {

  private String _exportRouteMap;

  private String _importRouteMap;

  private boolean _nextHopSelf;

  private int _remoteAs;

  private final Ip _remoteIp;

  public BgpNeighbor(Ip neighborIp) {
    _remoteIp = neighborIp;
  }

  public String getExportRouteMap() {
    return _exportRouteMap;
  }

  public String getImportRouteMap() {
    return _importRouteMap;
  }

  public boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public int getRemoteAs() {
    return _remoteAs;
  }

  public Ip getRemoteIp() {
    return _remoteIp;
  }

  public void setExportRouteMap(String exportRouteMap) {
    _exportRouteMap = exportRouteMap;
  }

  public void setImportRouteMap(String importRouteMap) {
    _importRouteMap = importRouteMap;
  }

  public void setNextHopSelf(boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }

  public void setRemoteAs(int remoteAs) {
    _remoteAs = remoteAs;
  }
}
