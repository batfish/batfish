package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serial;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment;

/** NextHopExpr that gets the peer address of the BGP peer where the policy is being evaluated. */
public final class BgpPeerAddressNextHop extends NextHopExpr {

  private static final BgpPeerAddressNextHop INSTANCE = new BgpPeerAddressNextHop();

  public static BgpPeerAddressNextHop getInstance() {
    return INSTANCE;
  }

  private BgpPeerAddressNextHop() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof BgpPeerAddressNextHop;
  }

  @Override
  public @Nonnull NextHop evaluate(Environment env) {
    Optional<Ip> remoteIp = env.getRemoteIp();
    checkState(remoteIp.isPresent(), "Expected BGP session properties");
    return NextHopIp.of(remoteIp.get());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0x12345678;
    return result;
  }

  @JsonCreator
  private static BgpPeerAddressNextHop jsonCreator() {
    return INSTANCE;
  }

  @Serial
  private Object readResolve() {
    return INSTANCE;
  }
}
