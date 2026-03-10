package org.batfish.minesweeper.collectors;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchAny;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprReference;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link org.batfish.minesweeper.collectors.AsPathNameAsPathMatchExprCollector}. */
public class AsPathNameAsPathMatchExprCollectorTest {

  private static final String HOSTNAME = "hostname";
  private Configuration _baseConfig;
  private AsPathNameAsPathMatchExprCollector _collector;

  private static final String ASPATH_LST_1 = "lst1";
  private static final String ASPATH_LST_2 = "lst2";

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _baseConfig = cb.build();
    _baseConfig.setAsPathMatchExprs(
        ImmutableMap.of(
            ASPATH_LST_1, AsPathMatchRegex.of(" 40$"), ASPATH_LST_2, AsPathMatchRegex.of(" 30$")));
    nf.vrfBuilder().setOwner(_baseConfig).setName(Configuration.DEFAULT_VRF_NAME).build();

    _collector = new AsPathNameAsPathMatchExprCollector();
  }

  @Test
  public void testVisitAsPathMatchAny() {
    assertEquals(
        ImmutableSet.of(ASPATH_LST_1, ASPATH_LST_2),
        AsPathMatchAny.of(
                ImmutableList.of(
                    AsPathMatchExprReference.of(ASPATH_LST_1),
                    AsPathMatchExprReference.of(ASPATH_LST_2)))
            .accept(_collector, _baseConfig));
  }

  @Test
  public void testAsPathMatchExprReference() {

    AsPathMatchExprReference reference = AsPathMatchExprReference.of(ASPATH_LST_1);

    assertEquals(ImmutableSet.of(ASPATH_LST_1), reference.accept(_collector, _baseConfig));
  }

  @Test
  public void testAsPathMatchRegex() {
    assertEquals(ImmutableSet.of(), AsPathMatchRegex.of(" 40$").accept(_collector, _baseConfig));
  }
}
