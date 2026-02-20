package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** BGP process configuration for Huawei device. */
public class HuaweiBgpProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  private long _asNum;
  private @Nullable Ip _routerId;
  private final @Nonnull Map<Ip, HuaweiBgpPeer> _peers;

  public HuaweiBgpProcess(long asNum) {
    _asNum = asNum;
    _peers = new HashMap<>();
  }

  public long getAsNum() {
    return _asNum;
  }

  public void setAsNum(long asNum) {
    _asNum = asNum;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public @Nonnull Map<Ip, HuaweiBgpPeer> getPeers() {
    return _peers;
  }
}
