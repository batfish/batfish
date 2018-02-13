package org.batfish.symbolic.abstraction;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import org.batfish.bdp.BdpDataPlanePlugin;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BatfishCompressionTest {

  private SortedMap<String, Configuration> simpleNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Configuration c2 = cb.build();
    Configuration c3 = cb.build();
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Vrf v1 = vb.setOwner(c1).build();
    Vrf v2 = vb.setOwner(c2).build();
    Vrf v3 = vb.setOwner(c3).build();
    Prefix p12 = Prefix.parse("10.12.0.0/31");
    Prefix p23 = Prefix.parse("10.23.0.0/31");
    Interface.Builder ib = nf.interfaceBuilder().setActive(true);
    Interface i1 =
        ib.setOwner(c1)
            .setVrf(v1)
            .setAddress(new InterfaceAddress(p12.getStartIp(), p12.getPrefixLength()))
            .build();
    Interface i21 =
        ib.setOwner(c2)
            .setVrf(v2)
            .setAddress(new InterfaceAddress(p12.getEndIp(), p12.getPrefixLength()))
            .build();
    Interface i23 =
        ib.setAddress(new InterfaceAddress(p23.getStartIp(), p23.getPrefixLength())).build();
    Interface i3 =
        ib.setOwner(c3)
            .setVrf(v3)
            .setAddress(new InterfaceAddress(p23.getEndIp(), p23.getPrefixLength()))
            .build();
    StaticRoute s13 = StaticRoute.builder().setNetwork(p23).setNextHopIp(p12.getEndIp()).build();
    v1.getStaticRoutes().add(s13);
    StaticRoute s31 = StaticRoute.builder().setNetwork(p12).setNextHopIp(p23.getStartIp()).build();
    v3.getStaticRoutes().add(s31);

    return new TreeMap<>(
        ImmutableSortedMap.of(c1.getName(), c1, c2.getName(), c2, c3.getName(), c3));
  }

  private static void assertIsCompressedConfig(Configuration config) {
    config
        .getRoutingPolicies()
        .values()
        .forEach(
            p -> {
              assertEquals(p.getStatements().size(), 1);
              assertThat(p.getStatements().get(0), instanceOf(If.class));
              If i = (If) p.getStatements().get(0);
              assertNotNull(i.getGuard());
            });
  }

  private Map<String, Map<String, SortedSet<FibRow>>> getFibs(
      SortedMap<String, Configuration> configs) throws IOException {
    // make sure to reconstruct the network, since compression mutates it
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, tmp);
    BdpDataPlanePlugin bdpDataPlanePlugin = new BdpDataPlanePlugin();
    bdpDataPlanePlugin.initialize(batfish);
    batfish.registerDataPlanePlugin(bdpDataPlanePlugin, "bdp");
    batfish.computeDataPlane(false);
    return batfish.loadDataPlane().getFibs();
  }

  private Map<String, Configuration> compressNetwork(Map<String, Configuration> configs)
      throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    IBatfish batfish = BatfishTestUtils.getBatfish(new TreeMap<>(configs), tmp);
    return new BatfishCompressor(batfish).compress(new HeaderSpace());
  }

  /** Test that compression doesn't change the fibs for this network. */
  @Test
  public void testCompressionFibs_simpleNetwork() throws IOException {
    Map<String, Configuration> compressedConfigs = compressNetwork(simpleNetwork());
    Map<String, Map<String, SortedSet<FibRow>>> origFibs = getFibs(simpleNetwork());
    Map<String, Map<String, SortedSet<FibRow>>> compressedFibs =
        getFibs(new TreeMap<>(compressedConfigs));

    compressedConfigs.values().forEach(BatfishCompressionTest::assertIsCompressedConfig);
    assert origFibs.keySet().equals(compressedFibs.keySet());
    assertEquals(origFibs, compressedFibs);

    origFibs
        .entrySet()
        .forEach(
            entry -> {
              String router = entry.getKey();
              entry
                  .getValue()
                  .forEach(
                      (vrf, rows) -> {
                        assertEquals(rows, compressedFibs.get(router).get(vrf));
                      });
            });
  }

  /** Build a network that can be easily compressed. */
  private SortedMap<String, Configuration> compressibleNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Configuration c2 = cb.build();
    Configuration c3 = cb.build();
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Vrf v1 = vb.setOwner(c1).build();
    Vrf v3 = vb.setOwner(c3).build();
    vb.setOwner(c2).build(); // add a vrf to c2 too
    Prefix p = Prefix.parse("10.23.0.0/31");
    Interface.Builder ib = nf.interfaceBuilder().setActive(true);
    ib.setOwner(c1)
        .setVrf(v1)
        .setAddress(new InterfaceAddress(p.getStartIp(), p.getPrefixLength()))
        .build();

    ib.setOwner(c3)
        .setVrf(v3)
        .setAddress(new InterfaceAddress(p.getStartIp(), p.getPrefixLength()))
        .build();

    StaticRoute staticRoute =
        StaticRoute.builder().setNetwork(p).setNextHopIp(p.getEndIp()).build();
    v1.getStaticRoutes().add(staticRoute);
    v3.getStaticRoutes().add(staticRoute);

    return new TreeMap<>(
        ImmutableSortedMap.of(c1.getName(), c1, c2.getName(), c2, c3.getName(), c3));
  }

  /**
   * Test the following invariant: if a FIB appears on concrete router “r”, then a corresponding
   * abstract FIB appears on one of these representatives. For example, if there is a concrete FIB
   * from C to D, then there should be an abstract FIB from A to B, where A is in representatives(C)
   * and B is in representatives(D).
   */
  @Test
  public void testCompressionFibs_compressibleNetwork() throws IOException {
    Map<String, Configuration> compressedConfigs = compressNetwork(compressibleNetwork());
    Map<String, Map<String, SortedSet<FibRow>>> origFibs = getFibs(compressibleNetwork());
    Map<String, Map<String, SortedSet<FibRow>>> compressedFibs =
        getFibs(new TreeMap<>(compressedConfigs));

    compressedConfigs.values().forEach(BatfishCompressionTest::assertIsCompressedConfig);

    Set<String> removedRouters = Sets.difference(origFibs.keySet(), compressedFibs.keySet());
    assertEquals(removedRouters.size(), 1);

    // A subset matcher would be nice here.
    Set<String> addedRouters = Sets.difference(compressedFibs.keySet(), origFibs.keySet());
    assertEquals(addedRouters.size(), 0);

    // all FIB entries in compressed network should also exist in the original network
    compressedFibs
        .entrySet()
        .stream()
        .forEach(
            routerEntry -> {
              String router = routerEntry.getKey();
              routerEntry
                  .getValue()
                  .entrySet()
                  .stream()
                  .forEach(
                      vrfEntry -> {
                        String vrf = vrfEntry.getKey();
                        SortedSet<FibRow> rows = vrfEntry.getValue();
                        assertEquals(rows, origFibs.get(router).get(vrf));
                      });
            });
  }
}
