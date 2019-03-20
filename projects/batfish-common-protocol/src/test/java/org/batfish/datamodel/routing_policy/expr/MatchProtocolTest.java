package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.IsisRoute.Builder;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.isis.IsisLevel;
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
  public void testJsonSerialization() throws IOException {
    MatchProtocol mp = new MatchProtocol(RoutingProtocol.STATIC);
    assertThat(BatfishObjectMapper.clone(mp, MatchProtocol.class), equalTo(mp));
  }

  @Test
  public void testMatchesIsisAny() {
    // Setup
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname("n1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();
    Builder rb =
        IsisRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setArea("fakeArea")
            .setSystemId("invalidSystemId");

    MatchProtocol mp = new MatchProtocol(RoutingProtocol.ISIS_ANY);
    Environment.Builder eb = Environment.builder(c, Configuration.DEFAULT_VRF_NAME);

    assertTrue(
        "Matches ISIS_L1",
        mp.evaluate(
                eb.setOriginalRoute(
                        rb.setProtocol(RoutingProtocol.ISIS_L1).setLevel(IsisLevel.LEVEL_1).build())
                    .build())
            .getBooleanValue());
    assertTrue(
        "Matches ISIS_L2",
        mp.evaluate(
                eb.setOriginalRoute(
                        rb.setProtocol(RoutingProtocol.ISIS_L2).setLevel(IsisLevel.LEVEL_2).build())
                    .build())
            .getBooleanValue());
    assertTrue(
        "Matches ISIS_EL1",
        mp.evaluate(
                eb.setOriginalRoute(
                        rb.setProtocol(RoutingProtocol.ISIS_EL1)
                            .setLevel(IsisLevel.LEVEL_1)
                            .build())
                    .build())
            .getBooleanValue());
    assertTrue(
        "Matches ISIS_EL2",
        mp.evaluate(
                eb.setOriginalRoute(
                        rb.setProtocol(RoutingProtocol.ISIS_EL2)
                            .setLevel(IsisLevel.LEVEL_2)
                            .build())
                    .build())
            .getBooleanValue());
    assertThat(
        "Does not match ISIS_ANY",
        not(
            mp.evaluate(
                    eb.setOriginalRoute(
                            rb.setProtocol(RoutingProtocol.ISIS_ANY)
                                .setLevel(IsisLevel.LEVEL_1)
                                .build())
                        .build())
                .getBooleanValue()));
    assertThat(
        "Does not match STATIC",
        not(
            mp.evaluate(
                    eb.setOriginalRoute(
                            StaticRoute.builder()
                                .setNetwork(Prefix.parse("1.1.1.0/24"))
                                .setAdmin(1)
                                .setNextHopIp(Ip.parse("2.2.2.2"))
                                .build())
                        .build())
                .getBooleanValue()));
  }
}
