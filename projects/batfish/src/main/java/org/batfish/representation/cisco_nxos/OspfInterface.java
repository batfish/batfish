package org.batfish.representation.cisco_nxos;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nullable;

public final class OspfInterface implements Serializable {

  // https://www.cisco.com/c/m/en_us/techdoc/dc/reference/cli/nxos/commands/ospf/ip-ospf-dead-interval.html
  public static final int DEFAULT_DEAD_INTERVAL = 40; // s
  public static final int DEFAULT_HELLO_INTERVAL = 10; // s

  public @Nullable Long getArea() {
    return _area;
  }

  public void setArea(@Nullable Long area) {
    _area = area;
  }

  public int getDeadInterval() {
    return firstNonNull(_deadInterval, DEFAULT_DEAD_INTERVAL);
  }

  public void setDeadInterval(int deadInterval) {
    _deadInterval = deadInterval;
  }

  public int getHelloInterval() {
    return firstNonNull(_helloInterval, DEFAULT_HELLO_INTERVAL);
  }

  public void setHelloInterval(int helloInterval) {
    _helloInterval = helloInterval;
  }

  public @Nullable OspfNetworkType getNetwork() {
    return _network;
  }

  public void setNetwork(@Nullable OspfNetworkType network) {
    _network = network;
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
  private @Nullable Integer _deadInterval;
  private @Nullable Integer _helloInterval;
  private @Nullable OspfNetworkType _network;
  private @Nullable String _process;
}
