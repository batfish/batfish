package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.Environment;
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
    Ip tailIp = Ip.parse("2.2.2.2");
    BgpSessionProperties sessionProps =
        BgpSessionProperties.builder()
            .setHeadAs(11111L)
            .setTailAs(22222L)
            .setHeadIp(Ip.parse("1.1.1.1"))
            .setTailIp(tailIp)
            .build();
    Environment env = Environment.builder(C).setBgpSessionProperties(sessionProps).build();
    assertThat(INSTANCE.getNextHopIp(env), equalTo(tailIp));
  }

  @Test
  public void testEvaluate_noBgpSessionProperties() {
    _thrown.expectMessage("Expected BGP session properties");
    INSTANCE.getNextHopIp(Environment.builder(C).build());
  }
}
