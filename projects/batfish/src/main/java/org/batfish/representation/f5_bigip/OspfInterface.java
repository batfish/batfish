package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nullable;

public final class OspfInterface implements Serializable {

  public @Nullable Integer getCost() {
    return _cost;
  }

  public void setCost(Integer cost) {
    _cost = cost;
  }

  public @Nullable Integer getDeadIntervalS() {
    return _deadIntervalS;
  }

  public void setDeadIntervalS(@Nullable Integer deadIntervalS) {
    _deadIntervalS = deadIntervalS;
  }

  public @Nullable Integer getHelloIntervalS() {
    return _helloIntervalS;
  }

  public void setHelloIntervalS(@Nullable Integer helloIntervalS) {
    _helloIntervalS = helloIntervalS;
  }

  public @Nullable OspfNetworkType getNetwork() {
    return _network;
  }

  public void setNetwork(@Nullable OspfNetworkType network) {
    _network = network;
  }

  private @Nullable Integer _cost;
  private @Nullable Integer _deadIntervalS;
  private @Nullable Integer _helloIntervalS;
  private @Nullable OspfNetworkType _network;
}
