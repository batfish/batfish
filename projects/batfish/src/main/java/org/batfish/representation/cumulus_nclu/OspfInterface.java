package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Interface Ospf data */
public class OspfInterface implements Serializable {
  private @Nullable Long _ospfArea;
  private @Nullable OspfNetworkType _network;

  private @Nullable Boolean _passive;
  private @Nullable Integer _deadInterval;
  private @Nullable Integer _helloInterval;
  private @Nullable Integer _cost;

  // http://docs.frrouting.org/en/latest/ospfd.html
  public static int DEFAULT_OSPF_HELLO_INTERVAL = 10;
  public static int DEFAULT_OSPF_DEAD_INTERVAL = 40;

  public @Nullable OspfNetworkType getNetwork() {
    return _network;
  }

  public void setNetwork(@Nullable OspfNetworkType network) {
    _network = network;
  }

  public @Nullable Integer getCost() {
    return _cost;
  }

  public void setCost(@Nullable Integer cost) {
    _cost = cost;
  }

  @Nullable
  public Long getOspfArea() {
    return _ospfArea;
  }

  public void setOspfArea(@Nullable Long ospfArea) {
    _ospfArea = ospfArea;
  }

  @Nullable
  public Boolean getPassive() {
    return _passive;
  }

  public void setPassive(@Nullable Boolean passive) {
    _passive = passive;
  }

  @Nullable
  public Integer getDeadInterval() {
    return _deadInterval;
  }

  @Nullable
  public Integer getHelloInterval() {
    return _helloInterval;
  }

  public void setDeadInterval(@Nullable Integer deadInterval) {
    _deadInterval = deadInterval;
  }

  public void setHelloInterval(@Nullable Integer helloInterval) {
    _helloInterval = helloInterval;
  }
}
