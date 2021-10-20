package org.batfish.e2e.bgp.allowas_in;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasAsPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Table;
import java.io.IOException;
import java.util.Set;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** A test that a common leaf-spine pattern with allowas-in works. */
public class BgpAllowAsInTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testAllowasIn() throws IOException {
    String SPINE = "spine";
    String LEAF1 = "leaf1";
    String LEAF2 = "leaf2";
    Prefix PREFIX = Prefix.parse("20.0.0.0/24");
    IBatfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles("org/batfish/e2e/bgp/allowas_in", LEAF1, LEAF2, SPINE)
                .build(),
            _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.loadConfigurations(snapshot);
    batfish.computeDataPlane(snapshot);

    // Check that both BGP links came up (in both directions).
    BgpTopology topology = batfish.getTopologyProvider().getBgpTopology(snapshot);
    assertThat(topology.getGraph().edges(), hasSize(4));

    DataPlane dp = batfish.loadDataPlane(snapshot);
    Table<String, String, Set<Bgpv4Route>> routes = dp.getBgpRoutes();
    Table<String, String, Set<Bgpv4Route>> backupRoutes = dp.getBgpBackupRoutes();

    assertThat(
        routes.get(LEAF2, "default"),
        contains(
            allOf(
                hasPrefix(PREFIX),
                hasAsPath(equalTo(AsPath.empty())),
                hasNextHop(NextHopDiscard.instance()))));
    assertThat(backupRoutes.get(LEAF2, "default"), empty());

    assertThat(
        routes.get(LEAF1, "default"),
        contains(
            allOf(
                hasPrefix(PREFIX),
                hasProtocol(RoutingProtocol.IBGP),
                hasAsPath(equalTo(AsPath.empty())),
                hasNextHop(NextHopIp.of(Ip.parse("20.0.0.1"))))));
    assertThat(backupRoutes.get(LEAF1, "default"), empty());

    assertThat(routes.get(SPINE, "default"), empty());
    assertThat(backupRoutes.get(SPINE, "default"), empty());
  }
}
