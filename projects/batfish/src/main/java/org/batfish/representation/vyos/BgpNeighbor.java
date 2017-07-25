package org.batfish.representation.vyos;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;

public class BgpNeighbor extends ComparableStructure<Ip> {

  /** */
  private static final long serialVersionUID = 1L;

  private String _exportRouteMap;

  private String _importRouteMap;

  private boolean _nextHopSelf;

  private int _remoteAs;

  public BgpNeighbor(Ip neighborIp) {
    super(neighborIp);
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
    return _key;
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
