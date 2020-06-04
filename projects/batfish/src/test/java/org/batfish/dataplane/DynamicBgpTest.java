package org.batfish.dataplane;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.ValueGraph;
import java.io.IOException;
import java.util.List;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DynamicBgpTest {
  private static final String TESTRIGS_PREFIX = "org/batfish/dataplane/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  /** Test correct handling of dynamic BGP sessions */
  @Test
  public void testDynamicBgp() throws IOException {
    String testrigName = "bgp-dynamic-session";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3", "r4");

    /*
     *
     * Setup is as follows:
     *
     *              +--+r3
     *  r1+------+r2|
     *              +--+r4
     *
     * Where r2 has a dynamic session listening to r3 & r4; We expect r1 to get routes advertised by
     * r3 & r4
     */
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        batfish.getTopologyProvider().getBgpTopology(snapshot).getGraph();

    // Three sessions are established, so should see 6 directed edges.
    assertThat(bgpTopology.edges(), hasSize(6));

    // Ensure routing info has been exchanged, and routes from r3/r4 exist on r1
    GenericRib<AnnotatedRoute<AbstractRoute>> r1Rib =
        dp.getRibs().get("r1").get(Configuration.DEFAULT_VRF_NAME);
    assertThat(r1Rib.getRoutes(), hasItem(hasPrefix(Prefix.parse("9.9.9.33/32"))));
    assertThat(r1Rib.getRoutes(), hasItem(hasPrefix(Prefix.parse("9.9.9.44/32"))));
  }

  @Test
  public void testDynamicBgpWithoutUpdateSource() throws IOException {
    String testrigName = "bgp-dynamic-session-no-update-source";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3", "r4");

    /*
     * See test testDynamicBgp above for network setup
     */
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        batfish.getTopologyProvider().getBgpTopology(snapshot).getGraph();

    // Three sessions are established, so should see 6 directed edges.
    assertThat(bgpTopology.edges(), hasSize(6));

    // Ensure routing info has been exchanged, and routes from r3/r4 exist on r1
    GenericRib<AnnotatedRoute<AbstractRoute>> r1Rib =
        dp.getRibs().get("r1").get(Configuration.DEFAULT_VRF_NAME);
    assertThat(r1Rib.getRoutes(), hasItem(hasPrefix(Prefix.parse("3.3.3.3/32"))));
    assertThat(r1Rib.getRoutes(), hasItem(hasPrefix(Prefix.parse("4.4.4.4/32"))));
  }

  /** Test BGP session establishment if dynamic sessions have been misconfigured. */
  @Test
  public void testDynamicBgpMisconfigured() throws IOException {
    String testrigName = "bgp-dynamic-session-misconfigured";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3", "r4");

    /*
     *
     * Setup is as follows:
     *
     *              +--+r3
     *  r1+------+r2|
     *              +--+r4
     *
     * Where r2 has a dynamic session listening to r3 & r4;
     * BUT
     * r4 has been misconfigured and is outside of the prefix range.
     * We expect r1 to get routes advertised by r3 only
     */
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);
    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        batfish.getTopologyProvider().getBgpTopology(snapshot).getGraph();

    // Only two sessions are established (not r2 <--> r4), so should see 4 directed edges.
    assertThat(bgpTopology.edges(), hasSize(4));

    // Ensure routing info has been exchanged, and routes from r3/r4 exist on r1
    GenericRib<AnnotatedRoute<AbstractRoute>> r1Rib =
        dp.getRibs().get("r1").get(Configuration.DEFAULT_VRF_NAME);
    assertThat(r1Rib.getRoutes(), hasItem(hasPrefix(Prefix.parse("9.9.9.33/32"))));
    assertThat(r1Rib.getRoutes(), not(hasItem(hasPrefix(Prefix.parse("9.9.9.44/32")))));
  }
}
