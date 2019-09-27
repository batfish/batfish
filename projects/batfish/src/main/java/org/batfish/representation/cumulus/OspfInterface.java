package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Interface Ospf data */
public class OspfInterface implements Serializable {
  private @Nullable Long _ospfArea;
  private @Nullable OspfNetworkType _network;

  private @Nullable Boolean _passive;

  public @Nullable OspfNetworkType getNetwork() {
    return _network;
  }

  public void setNetwork(@Nullable OspfNetworkType network) {
    _network = network;
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
}
