package org.batfish.symbolic.abstraction;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.Map;
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

  /**
   * Test that compression doesn't change the fibs for this network.
   */
  @Test
  public void testCompressionFibs() throws IOException {
    Map<String, Map<String, SortedSet<FibRow>>> origFibs = getFibs(simpleNetwork());
    Map<String, Map<String, SortedSet<FibRow>>> compressedFibs =
        getFibs(new TreeMap<>(compressNetwork(simpleNetwork())));

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
}
