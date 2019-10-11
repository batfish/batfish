package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

/** NextHopExpr that gets the peer address of the BGP peer where the policy is being evaluated. */
public class BgpPeerAddressNextHop extends NextHopExpr {

  private static BgpPeerAddressNextHop _instance = new BgpPeerAddressNextHop();

  public static BgpPeerAddressNextHop getInstance() {
    return _instance;
  }

  private BgpPeerAddressNextHop() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof BgpPeerAddressNextHop;
  }

  @Nullable
  @Override
  public Ip getNextHopIp(Environment environment) {
    BgpSessionProperties sessionProps = environment.getBgpSessionProperties();
    checkState(sessionProps != null, "Expected BGP session properties");
    return sessionProps.getTailIp();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0x12345678;
    return result;
  }
}
