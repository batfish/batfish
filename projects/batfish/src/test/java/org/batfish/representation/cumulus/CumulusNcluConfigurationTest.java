package org.batfish.representation.cumulus;

import static org.batfish.representation.cumulus.CumulusNcluConfiguration.toCommunityList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.junit.Test;

/** Test for {@link CumulusNcluConfiguration}. */
public class CumulusNcluConfigurationTest {
  @Test
  public void testToInterface_active() {
    Interface vsIface = new Interface("swp1", CumulusInterfaceType.PHYSICAL, null, 1);
    org.batfish.datamodel.Interface viIface = new CumulusNcluConfiguration().toInterface(vsIface);
    assertTrue(viIface.getActive());
  }

  @Test
  public void testToInterface_inactive() {
    Interface vsIface = new Interface("swp1", CumulusInterfaceType.PHYSICAL, null, 1);
    vsIface.setDisabled(true);
    org.batfish.datamodel.Interface viIface = new CumulusNcluConfiguration().toInterface(vsIface);
    assertFalse(viIface.getActive());
  }

  @Test
  public void testToInterface_sub_active() {
    Interface vsIface = new Interface("swp1s1", CumulusInterfaceType.PHYSICAL, null, 1);
    org.batfish.datamodel.Interface viIface =
        new CumulusNcluConfiguration().toInterface(vsIface, "swp1");
    assertTrue(viIface.getActive());
  }

  @Test
  public void testToInterface_sub_inactive() {
    Interface vsIface = new Interface("swp1s1", CumulusInterfaceType.PHYSICAL, null, 1);
    vsIface.setDisabled(true);
    org.batfish.datamodel.Interface viIface =
        new CumulusNcluConfiguration().toInterface(vsIface, "swp1");
    assertFalse(viIface.getActive());
  }

  @Test
  public void testToRouteFilterLine() {
    IpPrefixListLine prefixListLine =
        new IpPrefixListLine(
            LineAction.PERMIT, 10, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30));

    RouteFilterLine rfl = CumulusNcluConfiguration.toRouteFilterLine(prefixListLine);
    assertThat(
        rfl,
        equalTo(
            new RouteFilterLine(
                LineAction.PERMIT, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30))));
  }

  @Test
  public void testToRouteFilter() {
    IpPrefixList prefixList = new IpPrefixList("name");
    prefixList
        .getLines()
        .put(
            10L,
            new IpPrefixListLine(
                LineAction.DENY, 10, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30)));
    prefixList
        .getLines()
        .put(
            20L,
            new IpPrefixListLine(
                LineAction.PERMIT, 20, Prefix.parse("10.0.2.1/24"), new SubRange(28, 31)));

    RouteFilterList rfl = CumulusNcluConfiguration.toRouteFilterList(prefixList);

    assertThat(
        rfl,
        equalTo(
            new RouteFilterList(
                "name",
                ImmutableList.of(
                    new RouteFilterLine(
                        LineAction.DENY, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30)),
                    new RouteFilterLine(
                        LineAction.PERMIT, Prefix.parse("10.0.2.1/24"), new SubRange(28, 31))))));
  }

  @Test
  public void testToIpCommunityList() {
    String name = "name";
    IpCommunityListExpanded ipCommunityList =
        new IpCommunityListExpanded(
            name,
            LineAction.PERMIT,
            ImmutableList.of(StandardCommunity.of(10000, 1), StandardCommunity.of(20000, 2)));
    CommunityList result = toCommunityList(ipCommunityList);
    assertThat(
        result,
        equalTo(
            new CommunityList(
                name,
                ImmutableList.of(
                    new CommunityListLine(
                        LineAction.PERMIT, new LiteralCommunity(StandardCommunity.of(10000, 1))),
                    new CommunityListLine(
                        LineAction.PERMIT, new LiteralCommunity(StandardCommunity.of(20000, 2)))),
                false)));
  }
}
