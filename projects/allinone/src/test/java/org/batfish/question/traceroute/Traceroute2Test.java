package org.batfish.question.traceroute;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link TracerouteQuestion}. */
public class Traceroute2Test {
  private static final String TAG = "tag";

  private static final String FAST_ETHERNET = "FastEthernet0/0";
  private static final String LOOPBACK = "Loopback0";
  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final Ip NODE1_FAST_ETHERNET_IP = new Ip("1.1.1.2");
  private static final Ip NODE1_LOOPBACK_IP = new Ip("1.1.1.1");
  private static final Ip NODE2_FAST_ETHERNET_IP = new Ip("1.1.1.3");
  private static final Ip NODE2_LOOPBACK_IP = new Ip("2.2.2.2");
  private static final String TESTRIGS_PREFIX = "org/batfish/allinone/testrigs/";
  private static final String TESTRIG_NAME = "specifiers-reachability";
  private static final List<String> TESTRIG_NODE_NAMES = ImmutableList.of(NODE1, NODE2);
  private static final String VRF = "default";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException thrown = ExpectedException.none();

  private Batfish _batfish;

  @Before
  public void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(TESTRIGS_PREFIX + TESTRIG_NAME, TESTRIG_NODE_NAMES)
                .build(),
            _folder);

    _batfish.computeDataPlane(false);
  }

  /*
   * Build a simple 1-node network with an ACL.
   */
  private static SortedMap<String, Configuration> aclNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    ImmutableSortedMap.Builder<String, Configuration> configs =
        new ImmutableSortedMap.Builder<>(Comparator.naturalOrder());

    Configuration c1 = cb.build();
    configs.put(c1.getHostname(), c1);

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    // destination interface
    nf.interfaceBuilder()
        .setAddress(new InterfaceAddress("1.1.1.0/31"))
        .setOwner(c1)
        .setOutgoingFilter(
            nf.aclBuilder()
                .setOwner(c1)
                .setLines(ImmutableList.of(IpAccessListLine.REJECT_ALL))
                .build())
        .setVrf(v1)
        .build();

    return configs.build();
  }
}
