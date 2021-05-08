package org.batfish.representation.cisco_nxos;

import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.toOspfDeadInterval;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.toOspfHelloInterval;
import static org.batfish.representation.cisco_nxos.CiscoNxosConfiguration.toRouteFilterList;
import static org.batfish.representation.cisco_nxos.OspfInterface.DEFAULT_DEAD_INTERVAL_S;
import static org.batfish.representation.cisco_nxos.OspfInterface.DEFAULT_HELLO_INTERVAL_S;
import static org.batfish.representation.cisco_nxos.OspfInterface.OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.RouteFilterList;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link CiscoNxosConfiguration} class */
public class CiscoNxosConfigurationTest {

  @Test
  public void testToOspfDeadIntervalExplicit() {
    OspfInterface ospf = new OspfInterface();
    ospf.setDeadIntervalS(7);
    // Explicitly set dead interval should be preferred over inference
    assertThat(toOspfDeadInterval(ospf), equalTo(7));
  }

  @Test
  public void testToOspfDeadIntervalFromHello() {
    OspfInterface ospf = new OspfInterface();
    int helloInterval = 1;
    ospf.setHelloIntervalS(helloInterval);
    // Since the dead interval is not set, it should be inferred as four times the hello interval
    assertThat(
        toOspfDeadInterval(ospf), equalTo(OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * helloInterval));
  }

  @Test
  public void testToOspfDeadIntervalDefault() {
    OspfInterface ospf = new OspfInterface();
    // Since the dead interval and hello interval are not set, it should be the default value
    assertThat(toOspfDeadInterval(ospf), equalTo(DEFAULT_DEAD_INTERVAL_S));
  }

  @Test
  public void testToOspfHelloIntervalExplicit() {
    OspfInterface ospf = new OspfInterface();
    ospf.setHelloIntervalS(7);
    // Explicitly set hello interval should be preferred over default
    assertThat(toOspfHelloInterval(ospf), equalTo(7));
  }

  @Test
  public void testToOspfHelloIntervalDefault() {
    OspfInterface ospf = new OspfInterface();
    // Since the hello interval is not set, it should be the default value
    assertThat(toOspfHelloInterval(ospf), equalTo(DEFAULT_HELLO_INTERVAL_S));
  }

  /** Check that source name and type is set when prefix list is converted to route filter list */
  @Test
  public void testToRouterFilterList_prefixList_source() {
    IpPrefixList plist = new IpPrefixList("name");
    RouteFilterList rfl = toRouteFilterList(plist);
    Assert.assertThat(rfl.getSourceName(), equalTo("name"));
    Assert.assertThat(
        rfl.getSourceType(), equalTo(CiscoNxosStructureType.IP_PREFIX_LIST.getDescription()));
  }
}
