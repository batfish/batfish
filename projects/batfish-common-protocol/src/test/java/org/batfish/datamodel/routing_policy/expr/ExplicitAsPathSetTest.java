package org.batfish.datamodel.routing_policy.expr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link ExplicitAsPathSet}. */
public class ExplicitAsPathSetTest {
  private static Environment buildEnvironment(AsPath path) {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.JUNIPER);
    Configuration c = cb.build();
    c.setVrfs(
        ImmutableMap.of(Configuration.DEFAULT_VRF_NAME, new Vrf(Configuration.DEFAULT_VRF_NAME)));
    return Environment.builder(c)
        .setOriginalRoute(
            Bgpv4Route.testBuilder()
                .setOriginatorIp(Ip.ZERO)
                .setOriginType(OriginType.INCOMPLETE)
                .setProtocol(RoutingProtocol.BGP)
                .setNetwork(Prefix.ZERO)
                .setAsPath(path)
                .build())
        .build();
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new ExplicitAsPathSet(new RegexAsPathSetElem("123")),
            new ExplicitAsPathSet(new RegexAsPathSetElem("123")))
        .addEqualityGroup(
            new ExplicitAsPathSet(new RegexAsPathSetElem("123"), new RegexAsPathSetElem("456")))
        .addEqualityGroup(new ExplicitAsPathSet(new RegexAsPathSetElem("456")))
        .testEquals();
  }

  @Test
  public void testOperation() {
    ExplicitAsPathSet expr = new ExplicitAsPathSet(new RegexAsPathSetElem("13$"));
    assertTrue(expr.matches(buildEnvironment(AsPath.ofSingletonAsSets(13L))));
    assertTrue(expr.matches(buildEnvironment(AsPath.ofSingletonAsSets(11L, 12L, 13L))));
    assertTrue(expr.matches(buildEnvironment(AsPath.ofSingletonAsSets(13L, 113L))));
    assertFalse(expr.matches(buildEnvironment(AsPath.ofSingletonAsSets(11L, 12L, 13L, 14L))));

    expr = new ExplicitAsPathSet(new RegexAsPathSetElem(" 13$"));
    assertTrue(expr.matches(buildEnvironment(AsPath.ofSingletonAsSets(13L))));
    assertTrue(expr.matches(buildEnvironment(AsPath.ofSingletonAsSets(11L, 12L, 13L))));
    assertFalse(expr.matches(buildEnvironment(AsPath.ofSingletonAsSets(13L, 113L))));
    assertFalse(expr.matches(buildEnvironment(AsPath.ofSingletonAsSets(11L, 12L, 13L, 14L))));
  }
}
