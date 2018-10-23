package org.batfish.dataplane;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibImpl;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link FibImpl} */
@RunWith(JUnit4.class)
public class FibImplTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  private static final Ip DST_IP = new Ip("3.3.3.3");
  private static final String NODE1 = "node1";
  private static final String FAST_ETHERNET_0 = "FastEthernet0/0";
  private static final InterfaceAddress NODE1_PHYSICAL_NETWORK = new InterfaceAddress("2.0.0.1/8");
  private static final Ip EXTERNAL_IP = new Ip("7.7.7.7");

  private NetworkFactory _nf;
  private Builder _cb;
  private Interface.Builder _ib;
  private Vrf.Builder _vb;

  private Configuration _config;
  private Vrf _vrf;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _ib = _nf.interfaceBuilder();
    _vb = _nf.vrfBuilder();
    _config = _cb.setHostname(NODE1).build();
    _vrf = _vb.setOwner(_config).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(_config).setVrf(_vrf);
  }

  @Test
  public void testNextHopInterfaceTakesPrecedence() throws IOException {
    _ib.setName(FAST_ETHERNET_0).setAddresses(NODE1_PHYSICAL_NETWORK).build();
    // Both next hop IP and interface, interface should take precendence
    _vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(new Prefix(DST_IP, 32))
                .setNextHopInterface(FAST_ETHERNET_0)
                .setNextHopIp(EXTERNAL_IP)
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(_config.getHostname(), _config), folder);
    batfish.computeDataPlane(false);
    Fib fib =
        batfish
            .loadDataPlane()
            .getFibs()
            .get(_config.getHostname())
            .get(Configuration.DEFAULT_VRF_NAME);

    assertThat(fib.getNextHopInterfaces(DST_IP), contains(FAST_ETHERNET_0));
  }

  @Test
  public void testNextHopIpIsResolved() throws IOException {
    _ib.setName(FAST_ETHERNET_0).setAddresses(NODE1_PHYSICAL_NETWORK).build();
    // only next hop interface
    _vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(new Prefix(DST_IP, 32))
                .setNextHopIp(new Ip("2.1.1.1"))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(_config.getHostname(), _config), folder);
    batfish.computeDataPlane(false);
    Fib fib =
        batfish
            .loadDataPlane()
            .getFibs()
            .get(_config.getHostname())
            .get(Configuration.DEFAULT_VRF_NAME);

    assertThat(fib.getNextHopInterfaces(DST_IP), contains(FAST_ETHERNET_0));
  }
}
