package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Environment}. */
@RunWith(JUnit4.class)
public class EnvironmentTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testConstructorAndGetter() {
    SortedSet<String> nodeBlacklist = Sets.newTreeSet();
    nodeBlacklist.add("node1");
    SortedMap<String, String> bgpTables = Maps.newTreeMap();
    bgpTables.put("bgpTable1", "table1Content");
    SortedMap<String, String> routingTables = Maps.newTreeMap();
    routingTables.put("routingTable1", "table1Content");
    SortedSet<BgpAdvertisement> bgpAdvertisements = Sets.newTreeSet();
    bgpAdvertisements.add(
        new BgpAdvertisement(
            BgpAdvertisementType.EBGP_SENT,
            Prefix.parse("1.1.1.1/24"),
            Ip.parse("1.1.1.1"),
            "srcNode",
            "srcVrf",
            Ip.parse("2.2.2.2"),
            "dstNode",
            "dstVrf",
            Ip.parse("3.3.3.3"),
            RoutingProtocol.BGP,
            OriginType.EGP,
            20,
            20,
            Ip.parse("0.0.0.0"),
            AsPath.of(Lists.newArrayList()),
            ImmutableSortedSet.of(),
            ImmutableSortedSet.of(),
            10));
    Environment e =
        new Environment(
            "testrig",
            Sets.newTreeSet(),
            Sets.newTreeSet(),
            nodeBlacklist,
            bgpTables,
            routingTables,
            bgpAdvertisements);
    assertThat(e.getTestrigName(), equalTo("testrig"));
    assertThat(e.getEdgeBlacklist(), equalTo(Sets.newHashSet()));
    assertThat(e.getInterfaceBlacklist(), equalTo(Sets.newHashSet()));
    assertThat(e.getNodeBlacklist(), equalTo(nodeBlacklist));
    assertThat(e.getBgpTables(), equalTo(bgpTables));
    assertThat(e.getRoutingTables(), equalTo(routingTables));
    assertThat(e.getExternalBgpAnnouncements(), equalTo(bgpAdvertisements));
  }

  @Test
  public void testToString() {
    Environment e =
        new Environment(
            "testrig",
            Sets.newTreeSet(),
            Sets.newTreeSet(),
            Sets.newTreeSet(),
            Maps.newTreeMap(),
            Maps.newTreeMap(),
            Sets.newTreeSet());
    assertThat(
        e.toString(),
        equalTo(
            "Environment{testrigName=testrig, "
                + "edgeBlacklist=[], interfaceBlacklist=[], "
                + "nodeBlacklist=[], bgpTables={}, routingTables={}, "
                + "externalBgpAnnouncements=[]}"));
  }
}
