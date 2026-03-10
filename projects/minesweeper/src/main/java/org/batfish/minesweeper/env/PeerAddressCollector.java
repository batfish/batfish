package org.batfish.minesweeper.env;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.expr.MatchPeerAddress;
import org.batfish.minesweeper.aspath.RoutingPolicyCollector;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collect all peer IP addresses (see {@link MatchPeerAddress}) in a {@link
 * org.batfish.datamodel.routing_policy.RoutingPolicy}.
 */
@ParametersAreNonnullByDefault
public class PeerAddressCollector extends RoutingPolicyCollector<Ip> {
  @Override
  public Set<Ip> visitMatchPeerAddress(
      MatchPeerAddress matchPeerAddress, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.copyOf(matchPeerAddress.getPeers());
  }
}
