package org.batfish.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.junit.Test;

/** Tests of {@link BgpRib} */
public class BgpRibTest {

  @Test
  public void testBestPathComparator() {
    BgpRib<Bgpv4Route> rib = new Bgpv4Rib(null, BgpTieBreaker.ROUTER_ID, 1, null, false);
    Bgpv4Route.Builder rb =
        Bgpv4Route.builder()
            .setNetwork(Prefix.ZERO)
            .setNextHopIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.IBGP);
    /*
    Create routes to compare. With non-ARRIVAL_ORDER tiebreaker, preference should be:
    1. Lowest originator IP
    2. Shortest cluster list
    3. Lowest receivedFromIp, nulls first (first two properties are nonnull)
    */
    List<Ip> decreasingIps = ImmutableList.of(Ip.parse("1.1.1.1"), Ip.ZERO);
    List<Set<Long>> decreasingLengthClusterLists =
        ImmutableList.of(ImmutableSet.of(1L), ImmutableSet.of());
    List<Bgpv4Route> ordered = new ArrayList<>();
    for (Ip originatorIp : decreasingIps) {
      rb.setOriginatorIp(originatorIp);
      for (Set<Long> clusterList : decreasingLengthClusterLists) {
        rb.setClusterList(clusterList);
        ordered.add(rb.setReceivedFromIp(null).build());
        for (Ip receivedFromIp : decreasingIps) {
          ordered.add(rb.setReceivedFromIp(receivedFromIp).build());
        }
      }
    }

    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(rib.bestPathComparator(ordered.get(i), (ordered.get(j)))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }
}
