package org.batfish.datamodel.routing_policy.expr;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

public class PeerAddressNextHop extends NextHopExpr {

  private static PeerAddressNextHop _instance = new PeerAddressNextHop();

  public static PeerAddressNextHop getInstance() {
    return _instance;
  }

  private static final long serialVersionUID = 1L;

  private PeerAddressNextHop() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof PeerAddressNextHop;
  }

  @Nullable
  @Override
  public Ip getNextHopIp(Environment environment) {
    return environment.getPeerAddress();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0x12345678;
    return result;
  }
}
