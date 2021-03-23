package org.batfish.minesweeper.communities;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.expr.MainRib;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link RibExprVarCollector}. */
public final class RibExprVarCollectorTest {
  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;

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
    assertThat(MainRib.instance().accept(RibExprVarCollector.instance(), _baseConfig), empty());
  }
}
