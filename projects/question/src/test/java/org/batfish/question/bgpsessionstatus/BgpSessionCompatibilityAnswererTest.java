package org.batfish.question.bgpsessionstatus;

import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_CONFIGURED_STATUS;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_LOCAL_AS;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_LOCAL_INTERFACE;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_LOCAL_IP;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_NODE;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_REMOTE_AS;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_REMOTE_IP;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_REMOTE_NODE;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_SESSION_TYPE;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_VRF;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.ConfiguredSessionStatus;
import org.junit.Test;

public class BgpSessionCompatibilityAnswererTest {

  /*
  Setup for all tests:
  node1 peers with node2.
  node1 has a static BGP peer from 1.1.1.1 to 2.2.2.2 (AS 1 to AS 2).
  node2 has a static BGP peer from 2.2.2.2 to 1.1.1.1 (AS 2 to AS 1).
  node3 has a static BGP peer from 3.3.3.3 to 3.3.3.4 (AS 3 to AS 3).
  node4 has a dynamic BGP peer from 3.3.3.4 to 3.3.3.0/30 (AS 3 to AS 3).
  This results in four rows, represented by ROW_1, ROW_2, ROW_3, and ROW_4.
   */

  private final IBatfish _batfish;

  private static final Ip IP1 = new Ip("1.1.1.1");
  private static final Ip IP2 = new Ip("2.2.2.2");
  private static final Ip IP3 = new Ip("3.3.3.3");
  private static final Ip IP4 = new Ip("3.3.3.4");

  private static final Row ROW_1 =
      Row.builder()
          .put(COL_CONFIGURED_STATUS, ConfiguredSessionStatus.UNIQUE_MATCH)
          .put(COL_LOCAL_INTERFACE, new NodeInterfacePair("node1", "iface"))
          .put(COL_LOCAL_IP, IP1)
          .put(COL_LOCAL_AS, 1L)
          .put(COL_NODE, new Node("node1"))
          .put(COL_REMOTE_AS, new SelfDescribingObject(Schema.LONG, 2L))
          .put(COL_REMOTE_NODE, new Node("node2"))
          .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, IP2))
          .put(COL_SESSION_TYPE, SessionType.EBGP_SINGLEHOP)
          .put(COL_VRF, "vrf")
          .build();

  private static final Row ROW_2 =
      Row.builder()
          .put(COL_CONFIGURED_STATUS, ConfiguredSessionStatus.UNIQUE_MATCH)
          .put(COL_LOCAL_INTERFACE, new NodeInterfacePair("node2", "iface"))
          .put(COL_LOCAL_IP, IP2)
          .put(COL_LOCAL_AS, 2L)
          .put(COL_NODE, new Node("node2"))
          .put(COL_REMOTE_AS, new SelfDescribingObject(Schema.LONG, 1L))
          .put(COL_REMOTE_NODE, new Node("node1"))
          .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, IP1))
          .put(COL_SESSION_TYPE, SessionType.EBGP_SINGLEHOP)
          .put(COL_VRF, "vrf")
          .build();

  private static final Row ROW_3 =
      Row.builder()
          .put(COL_CONFIGURED_STATUS, ConfiguredSessionStatus.UNIQUE_MATCH)
          .put(COL_LOCAL_INTERFACE, new NodeInterfacePair("node3", "iface"))
          .put(COL_LOCAL_IP, IP3)
          .put(COL_LOCAL_AS, 3L)
          .put(COL_NODE, new Node("node3"))
          .put(COL_REMOTE_AS, new SelfDescribingObject(Schema.LONG, 3L))
          .put(COL_REMOTE_NODE, new Node("node4"))
          .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, IP4))
          .put(COL_SESSION_TYPE, SessionType.IBGP)
          .put(COL_VRF, "vrf")
          .build();

  private static final Row ROW_4 =
      Row.builder()
          .put(COL_CONFIGURED_STATUS, ConfiguredSessionStatus.DYNAMIC_MATCH)
          .put(COL_LOCAL_INTERFACE, new NodeInterfacePair("node4", "iface"))
          .put(COL_LOCAL_IP, IP4)
          .put(COL_LOCAL_AS, 3L)
          .put(COL_NODE, new Node("node4"))
          .put(COL_REMOTE_AS, new SelfDescribingObject(Schema.LONG, 3L))
          .put(COL_REMOTE_NODE, new Node("node3"))
          .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, IP3))
          .put(COL_SESSION_TYPE, SessionType.IBGP)
          .put(COL_VRF, "vrf")
          .build();

  public BgpSessionCompatibilityAnswererTest() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration node1 = createConfiguration(cb, "node1", IP1, IP2, 1L, 2L);
    Configuration node2 = createConfiguration(cb, "node2", IP2, IP1, 2L, 1L);
    Configuration node3 = createConfiguration(cb, "node3", IP3, IP4, 3L, 3L);
    Configuration node4 =
        createConfigurationWithDynamicSession(cb, IP4, new Prefix(IP3, 24), ImmutableList.of(3L));

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of("node1", node1, "node2", node2, "node3", node3, "node4", node4);

    _batfish =
        new IBatfishTestAdapter() {
          @Override
          public SortedMap<String, Configuration> loadConfigurations() {
            return configurations;
          }

          @Override
          public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
            return configurations;
          }
        };
  }

  private static Configuration createConfiguration(
      Configuration.Builder cb,
      String nodeName,
      Ip localIp,
      Ip remoteIp,
      Long localAs,
      Long remoteAs) {

    Configuration node = cb.setHostname(nodeName).build();
    Interface iface = new Interface("iface", node, InterfaceType.PHYSICAL);
    iface.setAllAddresses(
        ImmutableList.of(new InterfaceAddress(localIp, Ip.numSubnetBitsToSubnetMask(32))));

    BgpActivePeerConfig peerConfig =
        BgpActivePeerConfig.builder()
            .setLocalAs(localAs)
            .setRemoteAs(remoteAs)
            .setLocalIp(localIp)
            .setPeerAddress(remoteIp)
            .build();

    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.setNeighbors(ImmutableSortedMap.of(new Prefix(remoteIp, 32), peerConfig));

    Vrf vrf1 = new Vrf("vrf");
    vrf1.setBgpProcess(bgpProcess);

    node.setVrfs(ImmutableMap.of("vrf", vrf1));
    node.setInterfaces(ImmutableSortedMap.of("iface", iface));
    return node;
  }

  private static Configuration createConfigurationWithDynamicSession(
      Configuration.Builder cb, Ip localIp, Prefix remotePrefix, List<Long> remoteAsList) {

    Configuration node = cb.setHostname("node4").build();
    Interface iface = new Interface("iface", node, InterfaceType.PHYSICAL);
    iface.setAllAddresses(
        ImmutableList.of(new InterfaceAddress(localIp, Ip.numSubnetBitsToSubnetMask(32))));

    BgpPassivePeerConfig peerConfig =
        BgpPassivePeerConfig.builder()
            .setLocalAs(3L)
            .setRemoteAs(remoteAsList)
            .setLocalIp(localIp)
            .setPeerPrefix(remotePrefix)
            .build();

    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.setPassiveNeighbors(ImmutableSortedMap.of(remotePrefix, peerConfig));

    Vrf vrf1 = new Vrf("vrf");
    vrf1.setBgpProcess(bgpProcess);

    node.setVrfs(ImmutableMap.of("vrf", vrf1));
    node.setInterfaces(ImmutableSortedMap.of("iface", iface));
    return node;
  }

  @Test
  public void testAnswer() {
    BgpSessionCompatibilityQuestion q = new BgpSessionCompatibilityQuestion();
    Multiset<Row> rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of(ROW_1, ROW_2, ROW_3, ROW_4)));
  }

  @Test
  public void testLimitNodes() {
    BgpSessionCompatibilityQuestion q =
        new BgpSessionCompatibilityQuestion(new NodesSpecifier("node1"), null, null, null);
    Multiset<Row> rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of(ROW_1)));
  }

  @Test
  public void testLimitRemoteNodes() {
    BgpSessionCompatibilityQuestion q =
        new BgpSessionCompatibilityQuestion(null, new NodesSpecifier("node1"), null, null);
    Multiset<Row> rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of(ROW_2)));
  }

  @Test
  public void testLimitStatus() {
    // Both sessions have status UNIQUE_MATCH
    BgpSessionCompatibilityQuestion q =
        new BgpSessionCompatibilityQuestion(
            null, null, ConfiguredSessionStatus.UNIQUE_MATCH.name(), null);
    Multiset<Row> rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of(ROW_1, ROW_2, ROW_3)));

    q =
        new BgpSessionCompatibilityQuestion(
            null, null, ConfiguredSessionStatus.UNKNOWN_REMOTE.name(), null);
    rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of()));
  }

  @Test
  public void testLimitType() {
    // Session between nodes 1 and 2 has type EBGP_SINGLEHOP
    BgpSessionCompatibilityQuestion q =
        new BgpSessionCompatibilityQuestion(null, null, null, SessionType.EBGP_SINGLEHOP.name());
    Multiset<Row> rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of(ROW_1, ROW_2)));

    // Session between nodes 3 and 4 has type IBGP
    q = new BgpSessionCompatibilityQuestion(null, null, null, SessionType.IBGP.name());
    rows = getQuestionResults(q);
    assertThat(rows, containsInAnyOrder(ROW_3, ROW_4));
  }

  private Multiset<Row> getQuestionResults(BgpSessionCompatibilityQuestion q) {
    TableAnswerElement answer =
        (TableAnswerElement) new BgpSessionCompatibilityAnswerer(q, _batfish).answer();
    return answer.getRows().getData();
  }
}
