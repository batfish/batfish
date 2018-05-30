package org.batfish.symbolic.ainterpreter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

public class RibEntry implements Comparable<RibEntry> {

  public static final String PROP_HOSTNAME = "hostname";

  public static final String PROP_FROM_HOSTNAME = "fromHostname";

  public static final String PROP_PREFIX = "prefix";

  public static final String PROP_PROTOCOL = "protocol";

  public static final String PROP_METRIC = "metric";

  public static final String PROP_ADMIN_DIST = "adminDistance";

  public static final String PROP_NEXT_HOP = "nextHopIp";

  private String _hostname;

  private Prefix _prefix;

  private RoutingProtocol _protocol;

  private int _metric;

  private int _adminDistance;

  private String _nextHopIp;

  private String _fromHostname;

  @JsonCreator
  public RibEntry(
      @JsonProperty(PROP_HOSTNAME) String hostname,
      @JsonProperty(PROP_PREFIX) Prefix prefix,
      @JsonProperty(PROP_PROTOCOL) RoutingProtocol proto,
      @JsonProperty(PROP_METRIC) int metric,
      @JsonProperty(PROP_ADMIN_DIST) int adminDist,
      @JsonProperty(PROP_NEXT_HOP) String nextHopIp,
      @JsonProperty(PROP_FROM_HOSTNAME) String fromHostname) {
    this._hostname = hostname;
    this._prefix = prefix;
    this._protocol = proto;
    this._metric = metric;
    this._adminDistance = adminDist;
    this._nextHopIp = nextHopIp;
    this._fromHostname = fromHostname;
  }

  public String getHostname() {
    return _hostname;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  public RoutingProtocol getProtocol() {
    return _protocol;
  }

  public String getFromHostname() {
    return _fromHostname;
  }

  public int getMetric() {
    return _metric;
  }

  public int getAdminDistance() {
    return _adminDistance;
  }

  public String getNextHopIp() {
    return _nextHopIp;
  }

  @Override
  public String toString() {
    String route;
    switch (_protocol) {
      case OSPF:
        route = "OspfRoute";
        break;
      case IBGP:
        route = "IbgpRoute";
        break;
      case BGP:
        route = "BgpRoute";
        break;
      case STATIC:
        route = "StaticRoute";
        break;
      case CONNECTED:
        route = "ConnectedRoute";
        break;
      case AGGREGATE:
        route = "AggregateRoute";
        break;
      default:
        throw new BatfishException("Unsupported protocol");
    }
    return route
        + "<"
        + _prefix
        + "> metric="
        + _metric
        + ", ad="
        + _adminDistance
        + ", nextHopIp="
        + _nextHopIp;
  }

  @Override
  public int compareTo(RibEntry that) {
    if (this._prefix.compareTo(that._prefix) < 0) {
      return -1;
    } else if (this._prefix.compareTo(that._prefix) > 0) {
      return 1;
    }

    if (this._protocol.compareTo(that._protocol) < 0) {
      return -1;
    } else if (this._protocol.compareTo(that._protocol) > 0) {
      return 1;
    }

    if (this._metric < that._metric) {
      return -1;
    } else if (this._metric > that._metric) {
      return 1;
    }

    if (this._adminDistance < that._adminDistance) {
      return -1;
    } else if (this._adminDistance > that._adminDistance) {
      return 1;
    }

    if (this._nextHopIp.compareTo(that._nextHopIp) < 0) {
      return -1;
    } else if (this._nextHopIp.compareTo(that._nextHopIp) > 0) {
      return 1;
    }

    if (this._fromHostname.compareTo(that._fromHostname) < 0) {
      return -1;
    } else if (this._fromHostname.compareTo(that._fromHostname) > 0) {
      return 1;
    }

    if (this._hostname.compareTo(that._hostname) < 0) {
      return -1;
    } else if (this._hostname.compareTo(that._hostname) > 0) {
      return 1;
    }

    return 0;
  }
}
