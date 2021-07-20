package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Represents the OSPF settings on an interface as specified in router OSPF stanza */
public class OspfInterfaceSettings implements Serializable {

  public Integer getCost() {
    return _cost;
  }

  /** Get the time (in seconds) to wait before neighbors are declared dead */
  @Nullable
  public Integer getDeadInterval() {
    return _deadInterval;
  }

  /** Get the time (in seconds) between sending hello messages to neighbors */
  @Nullable
  public Integer getHelloInterval() {
    return _helloInterval;
  }

  public int getHelloMultiplier() {
    return _helloMultiplier;
  }

  public @Nullable OspfNetworkType getNetworkType() {
    return _networkType;
  }

  @Nullable
  public Boolean getPassive() {
    return _passive;
  }

  public boolean getShutdown() {
    return _shutdown;
  }

  public void setCost(int cost) {
    _cost = cost;
  }

  public void setDeadInterval(int seconds) {
    _deadInterval = seconds;
  }

  public void setHelloInterval(int seconds) {
    _helloInterval = seconds;
  }

  public void setHelloMultiplier(int multiplier) {
    _helloMultiplier = multiplier;
  }

  public void setNetworkType(@Nullable OspfNetworkType networkType) {
    _networkType = networkType;
  }

  public void setPassive(@Nullable Boolean passive) {
    _passive = passive;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }

  private Integer _cost;
  @Nullable private Integer _deadInterval;
  @Nullable private Integer _helloInterval;
  private int _helloMultiplier;
  @Nullable private OspfNetworkType _networkType;
  @Nullable private Boolean _passive;
  private boolean _shutdown;
}
