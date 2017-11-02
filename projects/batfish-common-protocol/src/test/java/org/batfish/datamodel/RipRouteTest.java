package org.batfish.datamodel;

import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RipRouteTest {
  @Rule public ExpectedException _expectedException = ExpectedException.none();

  @Test
  public void testConstructorWrongMetric() {
    int adminCost = RoutingProtocol.RIP.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS);
    new RipInternalRoute(new Prefix("1.1.1.1/32"), new Ip("2.2.2.2"), adminCost, 16);
    _expectedException.expect(BatfishException.class);
    new RipInternalRoute(new Prefix("1.1.1.1/32"), new Ip("2.2.2.2"), adminCost, 17);
    new RipInternalRoute(new Prefix("1.1.1.1/32"), new Ip("2.2.2.2"), adminCost, -1);
  }
}
