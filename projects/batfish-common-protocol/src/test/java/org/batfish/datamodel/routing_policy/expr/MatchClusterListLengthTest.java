package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link MatchClusterListLength} */
public final class MatchClusterListLengthTest {

  @Test
  public void testJacksonSerialization() {
    BooleanExpr obj = MatchClusterListLength.of(IntComparator.EQ, new LiteralInt(1));
    assertThat(BatfishObjectMapper.clone(obj, BooleanExpr.class), equalTo(obj));
  }

  @Test
  public void testJavaSerialization() {
    BooleanExpr obj = MatchClusterListLength.of(IntComparator.EQ, new LiteralInt(1));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    MatchClusterListLength obj = MatchClusterListLength.of(IntComparator.EQ, new LiteralInt(1));
    new EqualsTester()
        .addEqualityGroup(obj, MatchClusterListLength.of(IntComparator.EQ, new LiteralInt(1)))
        .addEqualityGroup(MatchClusterListLength.of(IntComparator.GE, new LiteralInt(1)))
        .addEqualityGroup(MatchClusterListLength.of(IntComparator.EQ, new LiteralInt(0)))
        .testEquals();
  }

  @Test
  public void testEvaluate() {
    BooleanExpr matchClusterListLength_0 =
        MatchClusterListLength.of(IntComparator.EQ, new LiteralInt(0));
    BooleanExpr matchClusterListLength_1 =
        MatchClusterListLength.of(IntComparator.EQ, new LiteralInt(1));
    Configuration c =
        Configuration.builder()
            .setHostname("h")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Bgpv4Route baseBgpRoute =
        Bgpv4Route.builder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.IBGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("192.0.2.1")))
            .setNextHop(NextHopDiscard.instance())
            .build();
    {
      Bgpv4Route route = baseBgpRoute.toBuilder().setClusterList(ImmutableSet.of(10L)).build();
      Environment e = Environment.builder(c).setOriginalRoute(route).build();
      assertFalse(matchClusterListLength_0.evaluate(e).getBooleanValue());
      assertTrue(matchClusterListLength_1.evaluate(e).getBooleanValue());
    }
    {
      Bgpv4Route route = baseBgpRoute;
      Environment e = Environment.builder(c).setOriginalRoute(route).build();
      assertTrue(matchClusterListLength_0.evaluate(e).getBooleanValue());
      assertFalse(matchClusterListLength_1.evaluate(e).getBooleanValue());
    }
    {
      // route without cluster list is treated as having cluster list length 0
      ConnectedRoute route = new ConnectedRoute(Prefix.ZERO, "foo");
      Environment e = Environment.builder(c).setOriginalRoute(route).build();
      assertTrue(matchClusterListLength_0.evaluate(e).getBooleanValue());
      assertFalse(matchClusterListLength_1.evaluate(e).getBooleanValue());
    }
  }
}
