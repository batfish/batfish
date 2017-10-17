package org.batfish.bdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsEqual.equalTo;

import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RipInternalRoute;
import org.batfish.datamodel.RoutingProtocol;
import org.junit.Before;
import org.junit.Test;

public class RipInternalRibTest {
  private RipInternalRib _rib;

  @Before
  public void setUp() {
    _rib = new RipInternalRib(null);
  }

  @Test
  public void testComparePreference() {
    RipInternalRoute r1 = new RipInternalRoute(
        new Prefix("10.1.0.0/16"),
        Ip.AUTO,
        RoutingProtocol.RIP.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS),
        100);
    RipInternalRoute r2 = new RipInternalRoute(
        new Prefix("10.1.0.0/16"),
        Ip.AUTO,
        RoutingProtocol.RIP.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS),
        200);

    assertThat(_rib.comparePreference(r1, r1), equalTo(0));
    assertThat(_rib.comparePreference(r1, r2), greaterThan(0));
    assertThat(_rib.comparePreference(r2, r1), lessThan(0));
  }
}
