package org.batfish.question.bidirectionalreachability;

import static org.batfish.question.bidirectionalreachability.BidirectionalReachabilityAnswerer.getAnswerBdds;
import static org.batfish.question.bidirectionalreachability.ReturnFlowType.FAILURE;
import static org.batfish.question.bidirectionalreachability.ReturnFlowType.MULTIPATH_INCONSISTENT;
import static org.batfish.question.bidirectionalreachability.ReturnFlowType.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.Location;
import org.junit.Test;

/**
 * Tests of {@link org.batfish.question.bidirectionalreachability.BidirectionalReachabilityAnswerer}
 */
public final class BidirectionalReachabilityAnswererTest {

  @Test
  public void testGetAnswerBdds() {
    BDDPacket pkt = new BDDPacket();
    BDDInteger var = pkt.getDstIp();
    BDD bdd1 = var.value(1);
    BDD bdd2 = var.value(2);
    BDD bdd3 = var.value(3);

    Location loc = new InterfaceLocation("node", "iface");

    // Simple success
    {
      Map<Location, BDD> success = ImmutableMap.of(loc, bdd1);
      BidirectionalReachabilityResult result =
          new BidirectionalReachabilityResult(success, ImmutableMap.of());
      assertEquals(getAnswerBdds(result, SUCCESS), success);
      assertTrue(getAnswerBdds(result, FAILURE).isEmpty());
      assertTrue(getAnswerBdds(result, MULTIPATH_INCONSISTENT).isEmpty());
    }

    // Simple failure
    {
      Map<Location, BDD> failure = ImmutableMap.of(loc, bdd1);
      BidirectionalReachabilityResult result =
          new BidirectionalReachabilityResult(ImmutableMap.of(), failure);
      assertTrue(getAnswerBdds(result, SUCCESS).isEmpty());
      assertEquals(getAnswerBdds(result, FAILURE), failure);
      assertTrue(getAnswerBdds(result, MULTIPATH_INCONSISTENT).isEmpty());
    }

    // Success/failure should prefer multipath consistent results
    {
      Map<Location, BDD> success = ImmutableMap.of(loc, bdd1.or(bdd2));
      Map<Location, BDD> failure = ImmutableMap.of(loc, bdd2.or(bdd3));
      BidirectionalReachabilityResult result =
          new BidirectionalReachabilityResult(success, failure);
      assertEquals(getAnswerBdds(result, SUCCESS), ImmutableMap.of(loc, bdd1));
      assertEquals(getAnswerBdds(result, FAILURE), ImmutableMap.of(loc, bdd3));
      assertEquals(getAnswerBdds(result, MULTIPATH_INCONSISTENT), ImmutableMap.of(loc, bdd2));
    }

    /* If no multipath consistent results exist, success/failure should fall fall back to
     * inconsistent ones.
     */
    {
      Map<Location, BDD> successAndFailure = ImmutableMap.of(loc, bdd1);
      BidirectionalReachabilityResult result =
          new BidirectionalReachabilityResult(successAndFailure, successAndFailure);
      assertEquals(getAnswerBdds(result, SUCCESS), successAndFailure);
      assertEquals(getAnswerBdds(result, FAILURE), successAndFailure);
      assertEquals(getAnswerBdds(result, MULTIPATH_INCONSISTENT), successAndFailure);
    }
  }
}
