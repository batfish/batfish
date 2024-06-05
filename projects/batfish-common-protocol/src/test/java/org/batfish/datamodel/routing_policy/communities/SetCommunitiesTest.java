package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Test of {@link SetCommunities}. */
public final class SetCommunitiesTest {

  private static final SetCommunities OBJ = new SetCommunities(new CommunitySetExprReference("a"));

  private static @Nonnull BgpRoute.Builder<?, ?> routeBuilder() {
    return Bgpv4Route.testBuilder()
        .setNetwork(Prefix.ZERO)
        .setOriginatorIp(Ip.ZERO)
        .setOriginType(OriginType.INCOMPLETE)
        .setProtocol(RoutingProtocol.BGP);
  }

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, SetCommunities.class), equalTo(OBJ));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(OBJ, OBJ, new SetCommunities(new CommunitySetExprReference("a")))
        .addEqualityGroup(new SetCommunities(new CommunitySetExprReference("b")))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testExecute() {
    Configuration c = new Configuration("h", ConfigurationFormat.CISCO_IOS);
    CommunitySet cs = CommunitySet.of(StandardCommunity.of(1L));
    SetCommunities set = new SetCommunities(new LiteralCommunitySet(cs));

    // test writing to output route
    Environment envWriteToOutput = Environment.builder(c).setOutputRoute(routeBuilder()).build();
    set.execute(envWriteToOutput);
    Bgpv4Route routeWriteToOutput = (Bgpv4Route) envWriteToOutput.getOutputRoute().build();
    assertThat(routeWriteToOutput.getCommunities().getCommunities(), equalTo(cs.getCommunities()));

    // test writing to intermediate bgp attributes
    Environment envWriteToIntermediate =
        Environment.builder(c)
            .setIntermediateBgpAttributes(routeBuilder())
            .setOutputRoute(routeBuilder())
            .setWriteToIntermediateBgpAttributes(true)
            .build();
    set.execute(envWriteToIntermediate);
    Bgpv4Route routeWriteToIntermediate =
        (Bgpv4Route) envWriteToIntermediate.getIntermediateBgpAttributes().build();
    assertThat(
        routeWriteToIntermediate.getCommunities().getCommunities(), equalTo(cs.getCommunities()));
  }
}
