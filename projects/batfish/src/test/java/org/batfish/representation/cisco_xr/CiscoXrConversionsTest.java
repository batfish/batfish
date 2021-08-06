package org.batfish.representation.cisco_xr;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlags;
import static org.batfish.datamodel.Names.generatedOspfInboundDistributeListName;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.getOspfInboundDistributeListPolicy;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toRouteFilterList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class CiscoXrConversionsTest extends TestCase {

  /** Check that vendorStructureId is set when ACL is converted to route filter list */
  @Test
  public void testToRouterFilterList_standardAccessList_vendorStructureId() {
    Ipv4AccessList acl = new Ipv4AccessList("name");
    RouteFilterList rfl = toRouteFilterList(acl, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", CiscoXrStructureType.IPV4_ACCESS_LIST.getDescription(), "name")));
  }

  /** Check that vendorStructureId is set when prefix list is converted to route filter list */
  @Test
  public void testToRouterFilterList_prefixList_vendorStructureId() {
    PrefixList plist = new PrefixList("name");
    RouteFilterList rfl = toRouteFilterList(plist, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", CiscoXrStructureType.PREFIX_LIST.getDescription(), "name")));
  }

  @Test
  public void testGetOspfInboundDistributeListPolicy_nullList() {
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS_XR);
    Warnings w = new Warnings(true, true, true);
    assertNull(getOspfInboundDistributeListPolicy(null, "vrf", "proc", 0, "iface", c, w));
    assertThat(w, hasRedFlags(empty()));
  }

  @Test
  public void testGetOspfInboundDistributeListPolicy_undefinedAcl() {
    DistributeList distList =
        new DistributeList("acl", DistributeList.DistributeListFilterType.ACCESS_LIST);
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS_XR);
    Warnings w = new Warnings(true, true, true);
    assertNull(getOspfInboundDistributeListPolicy(distList, "vrf", "proc", 0, "iface", c, w));
    assertThat(
        w,
        hasRedFlags(
            contains(
                hasText(
                    String.format(
                        "Ignoring OSPF distribute-list %s: access-list is not defined or failed to"
                            + " convert",
                        distList.getFilterName())))));
  }

  @Test
  public void testGetOspfInboundDistributeListPolicy_undefinedRoutePolicy() {
    DistributeList distList =
        new DistributeList("rp", DistributeList.DistributeListFilterType.ROUTE_POLICY);
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS_XR);
    Warnings w = new Warnings(true, true, true);
    assertNull(getOspfInboundDistributeListPolicy(distList, "vrf", "proc", 0, "iface", c, w));
    assertThat(
        w,
        hasRedFlags(
            contains(
                hasText(
                    String.format(
                        "Ignoring OSPF distribute-list %s: route-policy is not defined or failed to"
                            + " convert",
                        distList.getFilterName())))));
  }

  @Test
  public void testGetOspfInboundDistributeListPolicy_acl() {
    DistributeList distList =
        new DistributeList("acl", DistributeList.DistributeListFilterType.ACCESS_LIST);
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS_XR);
    Prefix permitted = Prefix.parse("1.1.1.0/24");
    RouteFilterList rfl =
        new RouteFilterList(
            distList.getFilterName(),
            ImmutableList.of(
                new RouteFilterLine(LineAction.PERMIT, PrefixRange.fromPrefix(permitted))));
    c.getRouteFilterLists().put(rfl.getName(), rfl);
    Warnings w = new Warnings(true, true, true);

    // Function should create a routing policy corresponding to the ACL's route filter list
    String vrfName = "vrf";
    String procName = "proc";
    long areaNum = 0;
    String ifaceName = "iface";
    String rpName = generatedOspfInboundDistributeListName(vrfName, procName, areaNum, ifaceName);
    assertThat(
        getOspfInboundDistributeListPolicy(distList, vrfName, procName, areaNum, ifaceName, c, w),
        equalTo(rpName));
    assertThat(w, hasRedFlags(empty()));

    // Test the generated routing policy
    RoutingPolicy generatedRp = c.getRoutingPolicies().get(rpName);
    StaticRoute permittedRoute = StaticRoute.testBuilder().setNetwork(permitted).build();
    StaticRoute deniedRoute = StaticRoute.testBuilder().setNetwork(Prefix.ZERO).build();
    assertTrue(generatedRp.process(permittedRoute, permittedRoute.toBuilder(), Direction.IN));
    assertFalse(generatedRp.process(deniedRoute, deniedRoute.toBuilder(), Direction.IN));
  }

  @Test
  public void testGetOspfInboundDistributeListPolicy_routePolicy() {
    DistributeList distList =
        new DistributeList("rp", DistributeList.DistributeListFilterType.ROUTE_POLICY);
    Configuration c = new Configuration("c", ConfigurationFormat.CISCO_IOS_XR);
    RoutingPolicy.builder().setName(distList.getFilterName()).setOwner(c).build();
    Warnings w = new Warnings(true, true, true);
    assertThat(
        getOspfInboundDistributeListPolicy(distList, "vrf", "proc", 0, "iface", c, w),
        equalTo(distList.getFilterName()));
    assertThat(w, hasRedFlags(empty()));
  }
}
