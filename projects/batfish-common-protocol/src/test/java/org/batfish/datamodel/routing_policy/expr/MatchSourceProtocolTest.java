package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link MatchSourceProtocol} */
public class MatchSourceProtocolTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new MatchSourceProtocol(RoutingProtocol.STATIC),
            new MatchSourceProtocol(RoutingProtocol.STATIC))
        .addEqualityGroup(new MatchSourceProtocol(RoutingProtocol.BGP))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    MatchSourceProtocol mp = new MatchSourceProtocol(RoutingProtocol.STATIC);
    assertThat(SerializationUtils.clone(mp), equalTo(mp));
  }

  @Test
  public void testJsonSerialization() {
    MatchSourceProtocol mp = new MatchSourceProtocol(RoutingProtocol.STATIC);
    assertThat(BatfishObjectMapper.clone(mp, BooleanExpr.class), equalTo(mp));
  }

  @Test
  public void testMatches() {
    // Setup
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname("n1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();

    MatchSourceProtocol mp = new MatchSourceProtocol(RoutingProtocol.CONNECTED);
    Environment.Builder eb = Environment.builder(c);

    StaticRoute noSourceProtocol =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAdmin(1)
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .build();
    assertFalse(mp.evaluate(eb.setOriginalRoute(noSourceProtocol).build()).getBooleanValue());

    Bgpv4Route connected =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setSrcProtocol(RoutingProtocol.CONNECTED)
            .build();
    assertTrue(mp.evaluate(eb.setOriginalRoute(connected).build()).getBooleanValue());

    assertFalse(
        mp.evaluate(eb.setOriginalRoute(connected.toBuilder().setSrcProtocol(null).build()).build())
            .getBooleanValue());
    assertFalse(
        mp.evaluate(
                eb.setOriginalRoute(
                        connected.toBuilder().setSrcProtocol(RoutingProtocol.LOCAL).build())
                    .build())
            .getBooleanValue());
  }
}
