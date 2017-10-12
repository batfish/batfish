package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
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

  @Test public void testConstructorAndGetter() {
    Set<String> nodeBlacklist = Sets.newHashSet("node1");
    Map<String, String> bgpTables = Collections.singletonMap("bgpTable1", "table1Content");
    Map<String, String> routingTables = Collections.singletonMap("routingTable1", "table1Content");
    Set<BgpAdvertisement> bgpAdvertisements = Sets.newHashSet(new BgpAdvertisement(
        BgpAdvertisementType.EBGP_SENT,
        new Prefix("1.1.1.1/24"),
        new Ip("1.1.1.1"),
        "srcNode",
        "srcVrf",
        new Ip("2.2.2.2"),
        "dstNode",
        "dstVrf",
        new Ip("3.3.3.3"),
        RoutingProtocol.BGP,
        OriginType.EGP,
        20,
        20,
        new Ip("0.0.0.0"),
        null,
        null,
        null,
        10));
    Environment e = new Environment(
        "environment",
        "testrig",
        Sets.newHashSet(),
        Sets.newHashSet(),
        nodeBlacklist,
        bgpTables,
        routingTables,
        bgpAdvertisements);
    assertThat(e.getEnvName(), equalTo("environment"));
    assertThat(e.getTestrigName(), equalTo("testrig"));
    assertThat(e.getEdgeBlacklist(), equalTo(Sets.newHashSet()));
    assertThat(e.getInterfaceBlacklist(), equalTo(Sets.newHashSet()));
    assertThat(e.getNodeBlacklist(), equalTo(nodeBlacklist));
    assertThat(e.getBgpTables(), equalTo(bgpTables));
    assertThat(e.getRoutingTables(), equalTo(routingTables));
    assertThat(e.getExternalBgpAnnouncements(), equalTo(bgpAdvertisements));
  }

  // does not seem like a useful test; commenting for now. remove?
  //  @Test
  //  public void testToString() {
  //    Environment e =
  //        new Environment(
  //            "environment",
  //            "testrig",
  //            Sets.newHashSet(),
  //            Sets.newHashSet(),
  //            Sets.newHashSet(),
  //            Maps.newHashMap(),
  //            Maps.newHashMap(),
  //            "announcement");
  //    assertThat(
  //        e.toString(),
  //        equalTo(
  //            "Environment{envName=environment, testrigName=testrig, "
  //                + "edgeBlacklist=[], interfaceBlacklist=[], "
  //                + "nodeBlacklist=[], bgpTables={}, routingTables={}, "
  //                + "externalBgpAnnouncements=announcement}"));
  //  }
}
