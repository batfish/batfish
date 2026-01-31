package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

public final class OspfInterface implements Serializable {

  // Default dead interval is hello interval times 4
  static final int OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER = 4;

  // https://www.cisco.com/c/m/en_us/techdoc/dc/reference/cli/nxos/commands/ospf/ip-ospf-dead-interval.html
  public static final int DEFAULT_HELLO_INTERVAL_S = 10; // s
  public static final int DEFAULT_DEAD_INTERVAL_S =
      OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * DEFAULT_HELLO_INTERVAL_S; // 40 s

  public @Nullable Long getArea() {
    return _area;
  }

  public void setArea(@Nullable Long area) {
    _area = area;
  }

  public boolean getBfd() {
    return _bfd;
  }

  public void setBfd(boolean bfd) {
    _bfd = bfd;
  }

  public @Nullable Integer getCost() {
    return _cost;
  }

  public void setCost(@Nullable Integer cost) {
    _cost = cost;
  }

  public @Nullable Integer getDeadIntervalS() {
    return _deadIntervalS;
  }

  public void setDeadIntervalS(int deadInterval) {
    _deadIntervalS = deadInterval;
  }

  public @Nullable Integer getHelloIntervalS() {
    return _helloIntervalS;
  }

  public void setHelloIntervalS(int helloIntervalS) {
    _helloIntervalS = helloIntervalS;
  }

  public @Nullable OspfNetworkType getNetwork() {
    return _network;
  }

  public void setNetwork(@Nullable OspfNetworkType network) {
    _network = network;
  }

  public @Nullable Boolean getPassive() {
    return _passive;
  }

  public void setPassive(@Nullable Boolean passive) {
    _passive = passive;
  }

  public @Nullable Integer getPriority() {
    return _priority;
  }

  public void setPriority(@Nullable Integer priority) {
    _priority = priority;
  }

  public @Nullable String getProcess() {
    return _process;
  }

  public void setProcess(@Nullable String process) {
    _process = process;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private @Nullable Long _area;
  private boolean _bfd;
  private @Nullable Integer _cost;
  private @Nullable Integer _deadIntervalS;
  private @Nullable Integer _helloIntervalS;
  private @Nullable OspfNetworkType _network;
  private @Nullable Boolean _passive;
  private @Nullable Integer _priority;
  private @Nullable String _process;
}
