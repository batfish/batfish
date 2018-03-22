package org.batfish.symbolic.abstraction;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.bdp.BdpDataPlanePlugin;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.hamcrest.Matchers;
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
    ib.setOwner(c1)
        .setVrf(v1)
        .setAddress(new InterfaceAddress(p12.getStartIp(), p12.getPrefixLength()))
        .build();
    ib.setOwner(c2)
        .setVrf(v2)
        .setAddress(new InterfaceAddress(p12.getEndIp(), p12.getPrefixLength()))
        .build();
    ib.setAddress(new InterfaceAddress(p23.getStartIp(), p23.getPrefixLength())).build();
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

  private DataPlane getDataPlane(SortedMap<String, Configuration> configs) throws IOException {
    // make sure to reconstruct the network, since compression mutates it
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, tmp);
    BdpDataPlanePlugin bdpDataPlanePlugin = new BdpDataPlanePlugin();
    bdpDataPlanePlugin.initialize(batfish);
    batfish.registerDataPlanePlugin(bdpDataPlanePlugin, "bdp");
    batfish.computeDataPlane(false);
    return batfish.loadDataPlane();
  }

  private SortedMap<String, Configuration> compressNetwork(
      Map<String, Configuration> configs, HeaderSpace headerSpace) throws IOException {
    TemporaryFolder tmp = new TemporaryFolder();
    tmp.create();
    IBatfish batfish = BatfishTestUtils.getBatfish(new TreeMap<>(configs), tmp);
    return new TreeMap<>(new BatfishCompressor(batfish, configs).compress(headerSpace));
  }

  /** Test that compression doesn't change the fibs for this network. */
  @Test
  public void testCompressionFibs_simpleNetwork() throws IOException {
    /* TODO: reimplement */
  }

  private Configuration _compressedNode1;

  private Configuration _compressedNode2;

  private Configuration _compressedNode3;

  /** Build a network that can be easily compressed. */
  private SortedMap<String, Configuration> compressibleNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _compressedNode1 = cb.build();
    _compressedNode2 = cb.build();
    _compressedNode3 = cb.build();
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Vrf v1 = vb.setOwner(_compressedNode1).build();
    Vrf v3 = vb.setOwner(_compressedNode3).build();
    vb.setOwner(_compressedNode2).build(); // add a vrf to c2 too
    Prefix p = Prefix.parse("10.23.0.0/31");
    Interface.Builder ib = nf.interfaceBuilder().setActive(true);
    ib.setOwner(_compressedNode1)
        .setVrf(v1)
        .setAddress(new InterfaceAddress(p.getStartIp(), p.getPrefixLength()))
        .build();

    ib.setOwner(_compressedNode3)
        .setVrf(v3)
        .setAddress(new InterfaceAddress(p.getStartIp(), p.getPrefixLength()))
        .build();

    StaticRoute staticRoute =
        StaticRoute.builder().setNetwork(p).setNextHopIp(p.getEndIp()).build();
    v1.getStaticRoutes().add(staticRoute);
    v3.getStaticRoutes().add(staticRoute);

    return new TreeMap<>(
        ImmutableSortedMap.of(
            _compressedNode1.getName(),
            _compressedNode1,
            _compressedNode2.getName(),
            _compressedNode2,
            _compressedNode3.getName(),
            _compressedNode3));
  }

  /**
   * Test the following invariant: if a FIB appears on concrete router “r”, then a corresponding
   * abstract FIB appears on one of these representatives. For example, if there is a concrete FIB
   * from C to D, then there should be an abstract FIB from A to B, where A is in representatives(C)
   * and B is in representatives(D).
   */
  @Test
  public void testCompressionFibs_compressibleNetwork() throws IOException {
    DataPlane origDataPlane = getDataPlane(compressibleNetwork());
    SortedMap<String, Configuration> compressedConfigs =
        compressNetwork(compressibleNetwork(), new HeaderSpace());
    DataPlane compressedDataPlane = getDataPlane(compressedConfigs);
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> origRibs = origDataPlane.getRibs();
    SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> compressedRibs = compressedDataPlane.getRibs();

    /* Compression removed a node */
    assertThat(compressedConfigs.entrySet(), hasSize(2));
    compressedConfigs.values().forEach(BatfishCompressionTest::assertIsCompressedConfig);
    compressedRibs.forEach(
        (hostname, compressedRibsByVrf) ->
            compressedRibsByVrf.forEach(
                (vrf, compressedRib) -> {
                  GenericRib<AbstractRoute> origRib = origRibs.get(hostname).get(vrf);
                  Set<AbstractRoute> origRoutes = origRib.getRoutes();
                  Set<AbstractRoute> compressedRoutes = compressedRib.getRoutes();
                  for (AbstractRoute route : compressedRoutes) {
                    /* Every compressed route should appear in original RIB */
                    assertThat(origRoutes, hasItem(route));
                  }
                }));
  }

  /**
   * This network should be compressed from: A --> B --> D, A --> C --> D to A --> {B,C} --> D.
   * i.e., B and C should be merged into one node.
   *
   * @return Configurations for the original (uncompressed) network.
   */
  private SortedMap<String, Configuration> diamondNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration cA = cb.setHostname("A").build();
    Configuration cB = cb.setHostname("B").build();
    Configuration cC = cb.setHostname("C").build();
    Configuration cD = cb.setHostname("D").build();

    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Vrf vA = vb.setOwner(cA).build();
    Vrf vB = vb.setOwner(cB).build();
    Vrf vC = vb.setOwner(cC).build();
    Vrf vD = vb.setOwner(cD).build();
    Prefix pAB = Prefix.parse("10.12.0.0/31");
    Prefix pAC = Prefix.parse("10.13.0.0/31");
    Prefix pBD = Prefix.parse("10.24.0.0/31");
    Prefix pCD = Prefix.parse("10.34.0.0/31");
    Interface.Builder ib = nf.interfaceBuilder().setActive(true);

    // Add a route from A --> B
    ib.setOwner(cA)
        .setVrf(vA)
        .setAddress(new InterfaceAddress(pAB.getStartIp(), pAB.getPrefixLength()))
        .build();
    // Interface iBA
    ib.setOwner(cB)
        .setVrf(vA)
        .setAddress(new InterfaceAddress(pAB.getEndIp(), pAB.getPrefixLength()))
        .build();
    // Interface iAC
    ib.setOwner(cA)
        .setVrf(vA)
        .setAddress(new InterfaceAddress(pAC.getStartIp(), pAC.getPrefixLength()))
        .build();
    // Interface iCA
    ib.setOwner(cC)
        .setVrf(vC)
        .setAddress(new InterfaceAddress(pAC.getEndIp(), pAC.getPrefixLength()))
        .build();
    // Interface iBD
    ib.setOwner(cB)
        .setVrf(vB)
        .setAddress(new InterfaceAddress(pBD.getStartIp(), pBD.getPrefixLength()))
        .build();
    // Interface iDB
    ib.setOwner(cD)
        .setVrf(vD)
        .setAddress(new InterfaceAddress(pBD.getEndIp(), pBD.getPrefixLength()))
        .build();
    // Interface iCD
    ib.setOwner(cC)
        .setVrf(vC)
        .setAddress(new InterfaceAddress(pCD.getStartIp(), pCD.getPrefixLength()))
        .build();
    // Interface iDC
    ib.setOwner(cD)
        .setVrf(vD)
        .setAddress(new InterfaceAddress(pCD.getEndIp(), pCD.getPrefixLength()))
        .build();

    // For the destination
    Prefix pD = Prefix.parse("4.4.4.4/32");
    // Interface iD
    ib.setOwner(cD)
        .setVrf(vD)
        .setAddress(new InterfaceAddress(pD.getEndIp(), pD.getPrefixLength()))
        .build();

    StaticRoute.Builder bld = StaticRoute.builder().setNetwork(pD);
    vA.getStaticRoutes().add(bld.setNextHopIp(pAB.getEndIp()).build());
    vA.getStaticRoutes().add(bld.setNextHopIp(pAC.getEndIp()).build());
    vB.getStaticRoutes().add(bld.setNextHopIp(pBD.getEndIp()).build());
    vC.getStaticRoutes().add(bld.setNextHopIp(pCD.getEndIp()).build());

    return new TreeMap<>(
        ImmutableSortedMap.of(
            cA.getName(), cA,
            cB.getName(), cB,
            cC.getName(), cC,
            cD.getName(), cD));
  }

  /**
   * Test the following invariant: if a FIB appears on concrete router “r”, then a corresponding
   * abstract FIB appears on one of these representatives. For example, if there is a concrete FIB
   * from C to D, then there should be an abstract FIB from A to B, where A is in representatives(C)
   * and B is in representatives(D).
   */
  @Test
  public void testCompressionFibs_diamondNetwork() throws IOException {
    /* TODO: reimplement */
  }
}
