package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link MatchProtocol} */
public class MatchProtocolTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new MatchProtocol(RoutingProtocol.STATIC), new MatchProtocol(RoutingProtocol.STATIC))
        .addEqualityGroup(new MatchProtocol(RoutingProtocol.BGP))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    MatchProtocol mp = new MatchProtocol(RoutingProtocol.STATIC);
    assertThat(SerializationUtils.clone(mp), equalTo(mp));
  }

  @Test
  public void testJsonSerialization() {
    MatchProtocol mp = new MatchProtocol(RoutingProtocol.STATIC);
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

    MatchProtocol mp = new MatchProtocol(RoutingProtocol.CONNECTED, RoutingProtocol.ISIS_L1);
    Environment.Builder eb = Environment.builder(c);

    StaticRoute staticR =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAdmin(1)
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .build();
    ConnectedRoute connected =
        ConnectedRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAdmin(1)
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setNextHopInterface("null0")
            .build();
    IsisRoute.Builder ib =
        IsisRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setArea("fakeArea")
            .setNextHop(NextHopDiscard.instance())
            .setSystemId("invalidSystemId");
    IsisRoute isisL1 = ib.setLevel(IsisLevel.LEVEL_1).setProtocol(RoutingProtocol.ISIS_L1).build();
    IsisRoute isisL2 = ib.setLevel(IsisLevel.LEVEL_2).setProtocol(RoutingProtocol.ISIS_L2).build();

    assertThat(
        "Matches CONNECTED",
        not(mp.evaluate(eb.setOriginalRoute(connected).build()).getBooleanValue()));
    assertThat(
        "Matches ISIS_L1", not(mp.evaluate(eb.setOriginalRoute(isisL1).build()).getBooleanValue()));

    assertThat(
        "Does not match STATIC",
        not(mp.evaluate(eb.setOriginalRoute(staticR).build()).getBooleanValue()));
    assertThat(
        "Does not match ISIS_L2",
        not(mp.evaluate(eb.setOriginalRoute(isisL2).build()).getBooleanValue()));
  }
}
