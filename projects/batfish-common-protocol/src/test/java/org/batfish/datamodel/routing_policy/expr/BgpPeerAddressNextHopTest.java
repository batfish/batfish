package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link BgpPeerAddressNextHop} */
public class BgpPeerAddressNextHopTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static final BgpPeerAddressNextHop INSTANCE = BgpPeerAddressNextHop.getInstance();
  private static final Configuration C =
      new NetworkFactory()
          .configurationBuilder()
          .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
          .build();

  @Test
  public void testEvaluate() {
    BgpSessionProperties sessionProps =
        BgpSessionProperties.builder()
            .setHeadAs(11111L)
            .setTailAs(22222L)
            .setHeadIp(Ip.parse("1.1.1.1"))
            .setTailIp(Ip.parse("2.2.2.2"))
            .build();
    Environment env =
        Environment.builder(C)
            .setBgpSessionProperties(sessionProps)
            .setDirection(Direction.IN)
            .build();
    assertThat(INSTANCE.evaluate(env), equalTo(NextHopIp.of(Ip.parse("2.2.2.2"))));
  }

  @Test
  public void testEvaluate_noBgpSessionProperties() {
    _thrown.expectMessage("Expected BGP session properties");
    INSTANCE.evaluate(Environment.builder(C).build());
  }
}
