package org.batfish.question.bgpproperties;

import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_CLUSTER_ID;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_EXPORT_POLICY;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_IMPORT_POLICY;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_LOCAL_AS;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_LOCAL_INTERFACE;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_LOCAL_IP;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_NODE;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_PEER_GROUP;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_REMOTE_AS;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_REMOTE_IP;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_ROUTE_REFLECTOR_CLIENT;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_SEND_COMMUNITY;
import static org.batfish.question.bgpproperties.BgpPeerConfigurationAnswerer.COL_VRF;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

public class BgpPeerConfigurationAnswererTest {

  @Test
  public void testAnswer() {
    Multiset<Row> rows =
        BgpPeerConfigurationAnswerer.getAnswerRows(
            ImmutableMap.of("c", getConfig()),
            ImmutableSet.of("c"),
            BgpPeerConfigurationAnswerer.createTableMetadata(new BgpPeerConfigurationQuestion(null))
                .toColumnMap());

    Node node = new Node("c");
    Multiset<Row> expected = HashMultiset.create();
    expected.add(
        Row.builder()
            .put(COL_NODE, node)
            .put(COL_VRF, "v")
            .put(COL_LOCAL_AS, 100L)
            .put(COL_LOCAL_INTERFACE, new NodeInterfacePair("c", "iface1"))
            .put(COL_LOCAL_IP, new Ip("1.1.1.1"))
            .put(COL_REMOTE_AS, new SelfDescribingObject(Schema.LONG, 200L))
            .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, new Ip("2.2.2.2")))
            .put(COL_ROUTE_REFLECTOR_CLIENT, false)
            .put(COL_CLUSTER_ID, null)
            .put(COL_PEER_GROUP, "g1")
            .put(COL_IMPORT_POLICY, ImmutableSet.of("p1"))
            .put(COL_EXPORT_POLICY, ImmutableSet.of("p2"))
            .put(COL_SEND_COMMUNITY, false)
            .build());
    expected.add(
        Row.builder()
            .put(COL_NODE, node)
            .put(COL_VRF, "v")
            .put(COL_LOCAL_AS, 100L)
            .put(COL_LOCAL_INTERFACE, new NodeInterfacePair("c", "iface2"))
            .put(COL_LOCAL_IP, new Ip("1.1.1.2"))
            .put(
                COL_REMOTE_AS,
                new SelfDescribingObject(Schema.list(Schema.LONG), ImmutableList.of(300L)))
            .put(
                COL_REMOTE_IP,
                new SelfDescribingObject(Schema.PREFIX, new Prefix(new Ip("3.3.3.0"), 24)))
            .put(COL_ROUTE_REFLECTOR_CLIENT, true)
            .put(COL_CLUSTER_ID, new Ip("5.5.5.5"))
            .put(COL_PEER_GROUP, "g2")
            .put(COL_IMPORT_POLICY, ImmutableSet.of("p3"))
            .put(COL_EXPORT_POLICY, ImmutableSet.of("p4"))
            .put(COL_SEND_COMMUNITY, false)
            .build());

    assertThat(rows, equalTo(expected));
  }

  private static Configuration getConfig() {
    Ip ip1 = new Ip("1.1.1.1");
    Ip ip2 = new Ip("1.1.1.2");

    Interface iface1 = new Interface("iface1");
    Interface iface2 = new Interface("iface2");
    iface1.setAllAddresses(ImmutableList.of(new InterfaceAddress(ip1, 32)));
    iface2.setAllAddresses(ImmutableList.of(new InterfaceAddress(ip2, 32)));

    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("c")
            .build();
    c.setInterfaces(ImmutableSortedMap.of("iface1", iface1, "iface2", iface2));

    BgpActivePeerConfig activePeer =
        BgpActivePeerConfig.builder()
            .setLocalAs(100L)
            .setRemoteAs(200L)
            .setLocalIp(ip1)
            .setPeerAddress(new Ip("2.2.2.2"))
            .setRouteReflectorClient(false)
            .setGroup("g1")
            .setImportPolicySources(ImmutableSortedSet.of("p1"))
            .setExportPolicySources(ImmutableSortedSet.of("p2"))
            .setSendCommunity(false)
            .build();
    BgpPassivePeerConfig passivePeer =
        BgpPassivePeerConfig.builder()
            .setLocalAs(100L)
            .setRemoteAs(ImmutableList.of(300L))
            .setLocalIp(ip2)
            .setPeerPrefix(new Prefix(new Ip("3.3.3.0"), 24))
            .setRouteReflectorClient(true)
            .setClusterId(new Ip("5.5.5.5").asLong())
            .setGroup("g2")
            .setImportPolicySources(ImmutableSortedSet.of("p3"))
            .setExportPolicySources(ImmutableSortedSet.of("p4"))
            .setSendCommunity(false)
            .build();

    BgpProcess process = new BgpProcess();
    process.setNeighbors(ImmutableSortedMap.of(new Prefix(new Ip("1.1.1.0"), 24), activePeer));
    process.setPassiveNeighbors(
        ImmutableSortedMap.of(new Prefix(new Ip("1.1.1.0"), 24), passivePeer));

    Vrf vrf = new Vrf("v");
    vrf.setBgpProcess(process);

    c.setVrfs(ImmutableMap.of("v", vrf, "emptyVrf", new Vrf("emptyVrf")));
    return c;
  }
}
