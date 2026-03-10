package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link MatchInterface} */
public class MatchInterfaceTest {
  @Test
  public void testSerialization() {
    MatchInterface m = new MatchInterface(ImmutableSet.of("i1", "i2"));
    assertThat(BatfishObjectMapper.clone(m, BooleanExpr.class), equalTo(m));
    assertThat(SerializationUtils.clone(m), equalTo(m));
  }

  @Test
  public void testEvaluate() {
    MatchInterface mi = new MatchInterface(ImmutableSet.of("e1", "e2"));
    ConnectedRoute e1 =
        ConnectedRoute.builder()
            .setNetwork(Prefix.parse("1.0.0.0/24"))
            .setNextHop(NextHopInterface.of("e1", Ip.parse("1.0.0.1")))
            .build();
    ConnectedRoute e2 =
        ConnectedRoute.builder()
            .setNetwork(Prefix.parse("2.0.0.0/24"))
            .setNextHop(NextHopInterface.of("e2", Ip.parse("2.0.0.1")))
            .build();
    ConnectedRoute e3 =
        ConnectedRoute.builder()
            .setNetwork(Prefix.parse("3.0.0.0/24"))
            .setNextHop(NextHopInterface.of("e3", Ip.parse("3.0.0.1")))
            .build();
    Configuration c =
        Configuration.builder()
            .setHostname("h")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    assertTrue(mi.evaluate(Environment.builder(c).setOriginalRoute(e1).build()).getBooleanValue());
    assertTrue(mi.evaluate(Environment.builder(c).setOriginalRoute(e2).build()).getBooleanValue());
    assertFalse(mi.evaluate(Environment.builder(c).setOriginalRoute(e3).build()).getBooleanValue());
  }
}
