package org.batfish.vendor.huawei.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** BGP peer configuration. */
public class HuaweiBgpPeer {

  private final @Nonnull Ip _ip;
  private @Nullable Long _asNum;

  public HuaweiBgpPeer(@Nonnull Ip ip) {
    _ip = ip;
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  public @Nullable Long getAsNum() {
    return _asNum;
  }

  public void setAsNum(@Nullable Long asNum) {
    _asNum = asNum;
  }
}
