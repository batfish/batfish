package org.batfish.question.bgpsessionstatus;

import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_CONFIGURED_STATUS;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_LOCAL_INTERFACE;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_LOCAL_IP;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_NODE;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_REMOTE_NODE;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_REMOTE_PREFIX;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_SESSION_TYPE;
import static org.batfish.question.bgpsessionstatus.BgpSessionCompatibilityAnswerer.COL_VRF_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import java.util.SortedMap;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.BgpActivePeerConfig;
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
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;
import org.junit.Test;

public class BgpSessionCompatibilityAnswererTest {

  /*
  Setup for all tests:
  node1 peers with node2.
  node1 has iface1 with IP 1.1.1.1 and vrf1 with BGP process from 1.1.1.1 to 2.2.2.2 (AS 1 to AS 2).
  node2 has iface2 with IP 2.2.2.2 and vrf2 with BGP process from 2.2.2.2 to 1.1.1.1 (AS 2 to AS 1).
  This results in two BGP sessions, represented by ROW_1 and ROW_2.
   */

  private final IBatfish _batfish;
  private static final Row ROW_1 =
      Row.builder()
          .put(COL_CONFIGURED_STATUS, SessionStatus.UNIQUE_MATCH)
          .put(COL_LOCAL_INTERFACE, new NodeInterfacePair("node1", "iface1"))
          .put(COL_LOCAL_IP, new Ip("1.1.1.1"))
          .put(COL_NODE, new Node("node1"))
          .put(COL_REMOTE_NODE, new Node("node2"))
          .put(COL_REMOTE_PREFIX, new Prefix(new Ip("2.2.2.2"), 32))
          .put(COL_SESSION_TYPE, SessionType.EBGP_SINGLEHOP)
          .put(COL_VRF_NAME, "vrf1")
          .build();

  private static final Row ROW_2 =
      Row.builder()
          .put(COL_CONFIGURED_STATUS, SessionStatus.UNIQUE_MATCH)
          .put(COL_LOCAL_INTERFACE, new NodeInterfacePair("node2", "iface2"))
          .put(COL_LOCAL_IP, new Ip("2.2.2.2"))
          .put(COL_NODE, new Node("node2"))
          .put(COL_REMOTE_NODE, new Node("node1"))
          .put(COL_REMOTE_PREFIX, new Prefix(new Ip("1.1.1.1"), 32))
          .put(COL_SESSION_TYPE, SessionType.EBGP_SINGLEHOP)
          .put(COL_VRF_NAME, "vrf2")
          .build();

  public BgpSessionCompatibilityAnswererTest() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Ip ip1 = new Ip("1.1.1.1");
    Ip ip2 = new Ip("2.2.2.2");

    Configuration node1 = cb.setHostname("node1").build();
    Interface iface1 = new Interface("iface1", node1, InterfaceType.VPN);
    iface1.setAllAddresses(ImmutableList.of(new InterfaceAddress(ip1, new Ip("255.255.255.255"))));

    BgpActivePeerConfig peerConfig1 =
        BgpActivePeerConfig.builder()
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setLocalIp(ip1)
            .setPeerAddress(ip2)
            .build();

    BgpProcess bgpProcess1 = new BgpProcess();
    bgpProcess1.setNeighbors(ImmutableSortedMap.of(new Prefix(ip2, 32), peerConfig1));

    Vrf vrf1 = new Vrf("vrf1");
    vrf1.setBgpProcess(bgpProcess1);

    node1.setVrfs(ImmutableMap.of("vrf1", vrf1));
    node1.setInterfaces(ImmutableSortedMap.of("iface1", iface1));

    Configuration node2 = cb.setHostname("node2").build();
    Interface iface2 = new Interface("iface2", node2, InterfaceType.VPN);
    iface2.setAllAddresses(ImmutableList.of(new InterfaceAddress(ip2, new Ip("255.255.255.255"))));

    BgpActivePeerConfig peerConfig2 =
        BgpActivePeerConfig.builder()
            .setLocalAs(2L)
            .setRemoteAs(1L)
            .setLocalIp(ip2)
            .setPeerAddress(ip1)
            .build();

    BgpProcess bgpProcess2 = new BgpProcess();
    bgpProcess2.setNeighbors(ImmutableSortedMap.of(new Prefix(ip1, 32), peerConfig2));

    Vrf vrf2 = new Vrf("vrf2");
    vrf2.setBgpProcess(bgpProcess2);

    node2.setVrfs(ImmutableMap.of("vrf2", vrf2));
    node2.setInterfaces(ImmutableSortedMap.of("iface2", iface2));

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of("node1", node1, "node2", node2);

    _batfish =
        new IBatfishTestAdapter() {
          @Override
          public SortedMap<String, Configuration> loadConfigurations() {
            return configurations;
          }
        };
  }

  @Test
  public void testAnswer() {
    BgpSessionCompatibilityQuestion q = new BgpSessionCompatibilityQuestion();
    Multiset<Row> rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of(ROW_1, ROW_2)));
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
        new BgpSessionCompatibilityQuestion(null, null, SessionStatus.UNIQUE_MATCH.name(), null);
    Multiset<Row> rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of(ROW_1, ROW_2)));

    q = new BgpSessionCompatibilityQuestion(null, null, SessionStatus.UNKNOWN_REMOTE.name(), null);
    rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of()));
  }

  @Test
  public void testLimitType() {
    // Both sessions have type EBGP_SINGLEHOP
    BgpSessionCompatibilityQuestion q =
        new BgpSessionCompatibilityQuestion(null, null, null, SessionType.EBGP_SINGLEHOP.name());
    Multiset<Row> rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of(ROW_1, ROW_2)));

    q = new BgpSessionCompatibilityQuestion(null, null, null, SessionType.EBGP_MULTIHOP.name());
    rows = getQuestionResults(q);
    assertThat(rows, equalTo(ImmutableMultiset.of()));
  }

  private Multiset<Row> getQuestionResults(BgpSessionCompatibilityQuestion q) {
    TableAnswerElement answer =
        (TableAnswerElement) new BgpSessionCompatibilityAnswerer(q, _batfish).answer();
    return answer.getRows().getData();
  }
}
