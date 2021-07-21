package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Common OSPF settings that can pertain to an OSPF process, area, or interface. */
public class OspfSettings implements Serializable {
  public @Nullable Integer getCost() {
    return _cost;
  }

  /** Get the time (in seconds) to wait before neighbors are declared dead */
  public @Nullable Integer getDeadInterval() {
    return _deadInterval;
  }

  /** Get the time (in seconds) between sending hello messages to neighbors */
  public @Nullable Integer getHelloInterval() {
    return _helloInterval;
  }

  public @Nullable OspfNetworkType getNetworkType() {
    return _networkType;
  }

  public @Nullable Boolean getPassive() {
    return _passive;
  }

  public void setCost(@Nullable Integer cost) {
    _cost = cost;
  }

  public void setDeadInterval(@Nullable Integer seconds) {
    _deadInterval = seconds;
  }

  public void setHelloInterval(@Nullable Integer seconds) {
    _helloInterval = seconds;
  }

  public void setNetworkType(@Nullable OspfNetworkType networkType) {
    _networkType = networkType;
  }

  public void setPassive(@Nullable Boolean passive) {
    _passive = passive;
  }

  /**
   * Sets any unset fields in this {@link OspfSettings} to their values in the given parent
   * settings. If a field is unset both here and in the parent, it will remain unset.
   */
  public void inheritFrom(OspfSettings parent) {
    if (_cost == null) {
      setCost(parent.getCost());
    }
    if (_deadInterval == null) {
      setDeadInterval(parent.getDeadInterval());
    }
    if (_helloInterval == null) {
      setHelloInterval(parent.getHelloInterval());
    }
    if (_networkType == null) {
      setNetworkType(parent.getNetworkType());
    }
    if (_passive == null) {
      setPassive(parent.getPassive());
    }
  }

  @Nullable private Integer _cost;
  @Nullable private Integer _deadInterval;
  @Nullable private Integer _helloInterval;
  @Nullable private OspfNetworkType _networkType;
  @Nullable private Boolean _passive;
}
