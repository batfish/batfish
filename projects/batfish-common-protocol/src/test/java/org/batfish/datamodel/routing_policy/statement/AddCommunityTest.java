package org.batfish.datamodel.routing_policy.statement;

import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasCommunities;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.junit.Test;

/** Tests of {@link AddCommunity} */
public class AddCommunityTest {
  @Test
  public void testEquals() {
    AddCommunity ac = new AddCommunity(new LiteralCommunity(StandardCommunity.of(1L)));
    new EqualsTester()
        .addEqualityGroup(ac, ac, new AddCommunity(new LiteralCommunity(StandardCommunity.of(1L))))
        .addEqualityGroup(new AddCommunity(new LiteralCommunity(StandardCommunity.of(2L))))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    AddCommunity ac = new AddCommunity(new LiteralCommunity(StandardCommunity.of(1L)));
    assertThat(SerializationUtils.clone(ac), equalTo(ac));
  }

  @Test
  public void testJsonSerialization() {
    AddCommunity ac = new AddCommunity(new LiteralCommunity(StandardCommunity.of(1L)));
    assertThat(BatfishObjectMapper.clone(ac, AddCommunity.class), equalTo(ac));
  }

  @Test
  public void testExecute() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    nf.vrfBuilder().setName("vrf").setOwner(c).build();
    AddCommunity ac = new AddCommunity(new LiteralCommunity(StandardCommunity.of(1L)));
    // Test does not crash on non-bgp route
    ac.execute(Environment.builder(c).setOutputRoute(ConnectedRoute.builder()).build());
    // Test sets community on BGP route
    Environment e = Environment.builder(c).setOutputRoute(Bgpv4Route.testBuilder()).build();
    ac.execute(e);
    assertThat(((Bgpv4Route.Builder) e.getOutputRoute()), hasCommunities(StandardCommunity.of(1L)));
  }
}
