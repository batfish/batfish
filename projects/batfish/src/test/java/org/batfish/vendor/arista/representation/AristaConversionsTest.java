package org.batfish.vendor.arista.representation;

import static org.batfish.vendor.arista.representation.AristaConversions.getAsnSpace;
import static org.batfish.vendor.arista.representation.AristaConversions.toOspfRedistributionProtocols;
import static org.batfish.vendor.arista.representation.Conversions.toRouteFilterList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.vendor.VendorStructureId;
import org.batfish.vendor.arista.representation.eos.AristaBgpPeerFilter;
import org.batfish.vendor.arista.representation.eos.AristaBgpPeerFilterLine;
import org.batfish.vendor.arista.representation.eos.AristaBgpV4DynamicNeighbor;
import org.junit.Test;

/** Tests of {@link AristaConversions} */
public class AristaConversionsTest {

  @Test
  public void testGetAsnSpaceSingularAs() {
    AristaBgpV4DynamicNeighbor neighbor =
        new AristaBgpV4DynamicNeighbor(Prefix.parse("1.1.1.0/24"));
    neighbor.setRemoteAs(1L);
    assertThat(getAsnSpace(neighbor, ImmutableMap.of()), equalTo(LongSpace.of(1)));
  }

  @Test
  public void testGetAsnSpaceNoAs() {
    AristaBgpV4DynamicNeighbor neighbor =
        new AristaBgpV4DynamicNeighbor(Prefix.parse("1.1.1.0/24"));
    assertThat(getAsnSpace(neighbor, ImmutableMap.of()), equalTo(LongSpace.EMPTY));
  }

  @Test
  public void testGetAsnSpaceUndefinedPeerList() {
    AristaBgpV4DynamicNeighbor neighbor =
        new AristaBgpV4DynamicNeighbor(Prefix.parse("1.1.1.0/24"));
    neighbor.setPeerFilter("PF");
    assertThat(getAsnSpace(neighbor, ImmutableMap.of()), equalTo(BgpPeerConfig.ALL_AS_NUMBERS));
  }

  @Test
  public void testGetAsnSpaceDefinedPeerList() {
    AristaBgpV4DynamicNeighbor neighbor =
        new AristaBgpV4DynamicNeighbor(Prefix.parse("1.1.1.0/24"));
    neighbor.setPeerFilter("PF");
    AristaBgpPeerFilter pf = new AristaBgpPeerFilter("PF");
    pf.addLine(LongSpace.of(1), AristaBgpPeerFilterLine.Action.ACCEPT);
    assertThat(getAsnSpace(neighbor, ImmutableMap.of(pf.getName(), pf)), equalTo(LongSpace.of(1)));
  }

  /** Check that vendorStructureId is set when extended ACL is converted to route filter list */
  @Test
  public void testToRouterFilterList_extendedAccessList_vendorStructureId() {
    ExtendedAccessList acl = new ExtendedAccessList("name");
    RouteFilterList rfl = toRouteFilterList(acl, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", AristaStructureType.IPV4_ACCESS_LIST_EXTENDED.getDescription(), "name")));
  }

  /** Check that vendorStructureId is set when standard ACL is converted to route filter list */
  @Test
  public void testToRouterFilterList_standardAccessList_vendorStructureId() {
    StandardAccessList acl = new StandardAccessList("name");
    RouteFilterList rfl = toRouteFilterList(acl, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", AristaStructureType.IP_ACCESS_LIST_STANDARD.getDescription(), "name")));
  }

  /** Check that source name and type is set when prefix list is converted to route filter list */
  @Test
  public void testToRouterFilterList_prefixList_vendorStructureId() {
    PrefixList plist = new PrefixList("name");
    RouteFilterList rfl = toRouteFilterList(plist, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", AristaStructureType.PREFIX_LIST.getDescription(), "name")));
  }

  @Test
  public void testToOspfRedistributionProtocols() {
    assertThat(
        toOspfRedistributionProtocols(RedistributionSourceProtocol.BGP_ANY),
        containsInAnyOrder(RoutingProtocol.BGP, RoutingProtocol.AGGREGATE, RoutingProtocol.IBGP));
    assertThat(
        toOspfRedistributionProtocols(RedistributionSourceProtocol.CONNECTED),
        containsInAnyOrder(RoutingProtocol.CONNECTED));
    assertThat(
        toOspfRedistributionProtocols(RedistributionSourceProtocol.ISIS_ANY),
        containsInAnyOrder(
            RoutingProtocol.ISIS_L1,
            RoutingProtocol.ISIS_L2,
            RoutingProtocol.ISIS_EL1,
            RoutingProtocol.ISIS_EL2));
    assertThat(
        toOspfRedistributionProtocols(RedistributionSourceProtocol.STATIC),
        containsInAnyOrder(RoutingProtocol.STATIC));
  }
}
