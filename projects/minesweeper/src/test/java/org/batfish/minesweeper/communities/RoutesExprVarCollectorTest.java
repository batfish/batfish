package org.batfish.minesweeper.communities;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.MainRibRoutes;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link RoutesExprVarCollector}. */
public final class RoutesExprVarCollectorTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private CommunitySetMatchExprVarCollector _varCollector;

  private static final Community COMM1 = StandardCommunity.parse("20:30");
  private static final Community COMM2 = StandardCommunity.parse("21:30");

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();
  }

  @Test
  public void testVisitMainRibRoutes() {
    assertThat(
        MainRibRoutes.instance().accept(RoutesExprVarCollector.instance(), _baseConfig), empty());
  }
}
