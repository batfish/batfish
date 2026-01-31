package org.batfish.vendor.cisco_nxos.representation;

import static org.batfish.vendor.cisco_nxos.representation.CiscoNxosConfiguration.toOspfDeadInterval;
import static org.batfish.vendor.cisco_nxos.representation.CiscoNxosConfiguration.toOspfHelloInterval;
import static org.batfish.vendor.cisco_nxos.representation.CiscoNxosConfiguration.toRouteFilterList;
import static org.batfish.vendor.cisco_nxos.representation.CiscoNxosConfiguration.toTraceableStatement;
import static org.batfish.vendor.cisco_nxos.representation.OspfInterface.DEFAULT_DEAD_INTERVAL_S;
import static org.batfish.vendor.cisco_nxos.representation.OspfInterface.DEFAULT_HELLO_INTERVAL_S;
import static org.batfish.vendor.cisco_nxos.representation.OspfInterface.OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.vendor.VendorStructureId;
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

  /** Check that vendorStructureId is set when prefix list is converted to route filter list */
  @Test
  public void testToRouterFilterList_prefixList_vendorStructureId() {
    IpPrefixList plist = new IpPrefixList("name");
    RouteFilterList rfl = toRouteFilterList(plist, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", CiscoNxosStructureType.IP_PREFIX_LIST.getDescription(), "name")));
  }

  /** Check that tracing is added to route map entries */
  @Test
  public void testToStatement_tracing() {
    CiscoNxosConfiguration cc = new CiscoNxosConfiguration();
    cc.setFilename("file");

    RouteMap map = new RouteMap("rm");
    RouteMapEntry entry = new RouteMapEntry(10);
    entry.setAction(LineAction.DENY);
    map.getEntries().put(10, entry);

    If statement = (If) cc.toStatement(map.getName(), entry, ImmutableMap.of(), ImmutableSet.of());

    TraceableStatement traceableStatement =
        (TraceableStatement) Iterables.getOnlyElement(statement.getTrueStatements());
    assertThat(
        traceableStatement.getTraceElement(),
        equalTo(toTraceableStatement(ImmutableList.of(), 10, "rm", "file").getTraceElement()));
    assertThat(statement.getFalseStatements(), equalTo(ImmutableList.of()));
  }
}
