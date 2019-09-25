package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nullable;

public final class OspfInterface implements Serializable {

  public @Nullable OspfNetworkType getNetwork() {
    return _network;
  }

  public void setNetwork(@Nullable OspfNetworkType network) {
    _network = network;
  }

  private @Nullable OspfNetworkType _network;
}
