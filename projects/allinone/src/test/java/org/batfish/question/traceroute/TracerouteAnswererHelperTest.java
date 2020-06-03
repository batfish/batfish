package org.batfish.question.traceroute;

import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressNode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressVrf;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIpProtocol;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link TracerouteAnswererHelper}. */
public class TracerouteAnswererHelperTest {
  private IBatfish _batfish;

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String FAST_ETHERNET = "FastEthernet0/0";
  private static final String LOOPBACK = "Loopback0";
  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final Ip NODE2_FAST_ETHERNET_IP = Ip.parse("1.1.1.3");
  private static final Ip NODE2_LOOPBACK_IP = Ip.parse("2.2.2.2");
  private static final String TESTRIGS_PREFIX = "org/batfish/allinone/testrigs/";
  private static final String TESTRIG_NAME = "specifiers-reachability";
  private static final List<String> TESTRIG_NODE_NAMES = ImmutableList.of(NODE1, NODE2);
  private static final String VRF = "default";

  private static final Ip NODE1_FAST_ETHERNET_IP = Ip.parse("1.1.1.2");
  private static final Ip NODE1_LOOPBACK_IP = Ip.parse("1.1.1.1");

  @Before
  public void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + TESTRIG_NAME, TESTRIG_NODE_NAMES)
                .build(),
            _folder);

    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  @Test
  public void testGetFlows_interfaceSource() {
    TracerouteAnswererHelper helper =
        new TracerouteAnswererHelper(
            PacketHeaderConstraints.builder().setDstIp("2.2.2.2").build(),
            NODE1,
            _batfish.specifierContext(_batfish.getSnapshot()));
    Set<Flow> flows = helper.getFlows();

    String ingressInterface = null;
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        hasItem(
            allOf(
                hasIngressInterface(ingressInterface),
                hasIngressNode("node1"),
                hasIngressVrf(VRF),
                hasSrcIp(NODE1_LOOPBACK_IP))));
    assertThat(
        flows,
        hasItem(
            allOf(
                hasIngressInterface(ingressInterface),
                hasIngressNode("node1"),
                hasIngressVrf(VRF),
                hasSrcIp(NODE1_FAST_ETHERNET_IP))));
  }

  @Test
  public void testGetFlows_interfaceLinkSourceNoSourceIp() {
    TracerouteAnswererHelper helper =
        new TracerouteAnswererHelper(
            PacketHeaderConstraints.builder().setDstIp("2.2.2.2").build(),
            "enter(node1)",
            _batfish.specifierContext(_batfish.getSnapshot()));
    thrown.expect(IllegalArgumentException.class);
    helper.getFlows();
  }

  @Test
  public void testGetFlows_interfaceLinkSource() {
    TracerouteAnswererHelper helper =
        new TracerouteAnswererHelper(
            PacketHeaderConstraints.builder().setSrcIp("1.1.1.0").setDstIp("2.2.2.2").build(),
            "enter(node1)",
            _batfish.specifierContext(_batfish.getSnapshot()));

    Set<Flow> flows = helper.getFlows();

    // neither interfaces have networks for which link (host) IPs can be inferred
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        hasItem(
            allOf(
                hasIngressInterface(LOOPBACK),
                hasIngressNode("node1"),
                hasIngressVrf(nullValue()),
                hasSrcIp(Ip.parse("1.1.1.0")),
                hasIpProtocol(IpProtocol.UDP))));
    assertThat(
        flows,
        hasItem(
            allOf(
                hasIngressInterface(FAST_ETHERNET),
                hasIngressNode("node1"),
                hasIngressVrf(nullValue()),
                hasSrcIp(Ip.parse("1.1.1.0")),
                hasIpProtocol(IpProtocol.UDP))));
  }

  @Test
  public void testGetFlows_dst() {
    TracerouteAnswererHelper helper =
        new TracerouteAnswererHelper(
            PacketHeaderConstraints.builder().setDstIp("ofLocation(node2)").build(),
            String.format("%s[%s]", NODE1, LOOPBACK),
            _batfish.specifierContext(_batfish.getSnapshot()));
    Set<Flow> flows = helper.getFlows();
    assertThat(
        flows, everyItem(anyOf(hasDstIp(NODE2_FAST_ETHERNET_IP), hasDstIp(NODE2_LOOPBACK_IP))));
  }
}
