package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.common.BatfishException;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class AbstractRoute6 implements Serializable {

  protected static final String PROP_ADMINISTRATIVE_COST = "administrativeCost";
  private static final String PROP_METRIC = "metric";

  protected static final String PROP_NETWORK = "network";

  protected static final String PROP_NEXT_HOP_IP = "nextHopIp";

  public static final int NO_TAG = -1;

  protected final Prefix6 _network;

  protected final Ip6 _nextHopIp;

  private boolean _nonRouting;

  public AbstractRoute6(Prefix6 network, Ip6 nextHopIp) {
    if (network == null) {
      throw new BatfishException("Cannot construct AbstractRoute with null network");
    }
    _network = network;
    _nextHopIp = nextHopIp;
  }

  @Override
  public abstract boolean equals(Object o);

  @JsonProperty(PROP_ADMINISTRATIVE_COST)
  public abstract int getAdministrativeCost();

  @JsonProperty(PROP_METRIC)
  public abstract Integer getMetric();

  @JsonProperty(PROP_NETWORK)
  public final Prefix6 getNetwork() {
    return _network;
  }

  public abstract String getNextHopInterface();

  @JsonProperty(PROP_NEXT_HOP_IP)
  public Ip6 getNextHopIp() {
    return _nextHopIp;
  }

  @JsonIgnore
  public final boolean getNonRouting() {
    return _nonRouting;
  }

  @JsonIgnore
  public abstract RoutingProtocol getProtocol();

  public abstract int getTag();

  @Override
  public abstract int hashCode();

  @JsonIgnore
  public final void setNonRouting(boolean nonRouting) {
    _nonRouting = nonRouting;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + _network + ">";
  }
}
