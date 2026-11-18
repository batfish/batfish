package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.testing.EqualsTester;
import java.util.Collections;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OspfExternalRoute;
import org.batfish.datamodel.OspfExternalType1Route;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests of {@link SetNextHop} */
class SetNextHopTest {
  private Configuration _c;

  @BeforeEach
  void setUp() {
    _c =
        new NetworkFactory()
            .configurationBuilder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.ARISTA)
            .build();
  }

  @Test
  void testNonBgpRoute() {
    SetNextHop expr = new SetNextHop(new IpNextHop(Collections.singletonList(Ip.ZERO)));
    OspfExternalRoute.Builder builder = OspfExternalType1Route.builder();
    expr.execute(Environment.builder(_c).setOutputRoute(builder).build());
    assertThat(builder.getNextHopIp(), nullValue());
  }

  @Test
  void testBgpRoute() {
    Ip ip = Ip.parse("1.1.1.1");
    SetNextHop expr = new SetNextHop(new IpNextHop(Collections.singletonList(ip)));
    Bgpv4Route.Builder builder = Bgpv4Route.testBuilder();
    expr.execute(Environment.builder(_c).setOutputRoute(builder).build());
    assertThat(builder.getNextHopIp(), equalTo(ip));
  }

  @Test
  void testEquals() {
    SetNextHop expr = new SetNextHop(new IpNextHop(Collections.singletonList(Ip.ZERO)));
    new EqualsTester()
        .addEqualityGroup(
            expr, expr, new SetNextHop(new IpNextHop(Collections.singletonList(Ip.ZERO))))
        .addEqualityGroup(new SetNextHop(new IpNextHop(Collections.singletonList(Ip.MAX))))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
