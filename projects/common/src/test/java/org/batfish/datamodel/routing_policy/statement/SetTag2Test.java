package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.junit.Test;

/** Tests of {@link SetTag2}. */
public final class SetTag2Test {
  private static final Configuration C =
      new NetworkFactory()
          .configurationBuilder()
          .setConfigurationFormat(ConfigurationFormat.JUNIPER)
          .build();
  private static final SetTag2 SET_7 = new SetTag2(new LiteralLong(7L));

  @Test
  public void testExecute() {
    Bgpv4Route route =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("10.0.0.0/8"))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();
    Bgpv4Route.Builder output = route.toBuilder();
    SET_7.execute(Environment.builder(C).setOriginalRoute(route).setOutputRoute(output).build());
    assertThat(output.build().getTag2(), equalTo(7L));
    assertThat(route.getTag2(), equalTo(0L));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(SET_7, new SetTag2(new LiteralLong(7L)))
        .addEqualityGroup(new SetTag2(new LiteralLong(8L)))
        .addEqualityGroup(new SetTag(new LiteralLong(7L)))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    assertThat(BatfishObjectMapper.clone(SET_7, Statement.class), equalTo(SET_7));
    assertThat(SerializationUtils.clone(SET_7), equalTo(SET_7));
  }
}
