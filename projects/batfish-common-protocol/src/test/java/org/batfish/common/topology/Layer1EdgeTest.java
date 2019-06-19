package org.batfish.common.topology;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.junit.Before;
import org.junit.Test;

public final class Layer1EdgeTest {
  private Configuration.Builder _cb;
  private Interface.Builder _ib;
  private NetworkFactory _nf;
  private Vrf.Builder _vb;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = _nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    _ib = _nf.interfaceBuilder();
  }

  @Test
  public void testToLogicalEdgeNode1Absent() {
    String c1Name = "c1";
    String c2Name = "c2";
    String iName = "i1";

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(iName).build().setChannelGroup("missing");

    Configuration c2 = _cb.setHostname(c2Name).build();
    Vrf v2 = _vb.setOwner(c2).build();
    _ib.setOwner(c2).setVrf(v2);
    _ib.setName(iName).build();

    NetworkConfigurations networkConfigurations =
        NetworkConfigurations.of(ImmutableSortedMap.of(c1Name, c1, c2Name, c2));
    Layer1Edge edge = new Layer1Edge(new Layer1Node(c1Name, iName), new Layer1Node(c2Name, iName));

    // If node1 of a layer-1 physical edge cannot be mapped to a layer-1 logical node, a null edge
    // should be returned.
    assertThat(edge.toLogicalEdge(networkConfigurations), nullValue());
  }

  @Test
  public void testToLogicalEdgeNode2Absent() {
    String c1Name = "c1";
    String c2Name = "c2";
    String iName = "i1";

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(iName).build();

    Configuration c2 = _cb.setHostname(c2Name).build();
    Vrf v2 = _vb.setOwner(c2).build();
    _ib.setOwner(c2).setVrf(v2);
    // force node2 to map to null
    _ib.setName(iName).build().setChannelGroup("missing");

    NetworkConfigurations networkConfigurations =
        NetworkConfigurations.of(ImmutableSortedMap.of(c1Name, c1, c2Name, c2));
    Layer1Edge edge = new Layer1Edge(new Layer1Node(c1Name, iName), new Layer1Node(c2Name, iName));

    // If node2 of a layer-1 physical edge cannot be mapped to a layer-1 logical node, a null edge
    // should be returned.
    assertThat(edge.toLogicalEdge(networkConfigurations), nullValue());
  }

  @Test
  public void testToLogicalEdgePresent() {
    String c1Name = "c1";
    String c2Name = "c2";
    String iName = "i1";

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(iName).build();

    Configuration c2 = _cb.setHostname(c2Name).build();
    Vrf v2 = _vb.setOwner(c2).build();
    _ib.setOwner(c2).setVrf(v2);
    _ib.setName(iName).build();

    NetworkConfigurations networkConfigurations =
        NetworkConfigurations.of(ImmutableSortedMap.of(c1Name, c1, c2Name, c2));
    Layer1Edge edge = new Layer1Edge(new Layer1Node(c1Name, iName), new Layer1Node(c2Name, iName));

    // If both nodes of a layer-1 physical edge, can be mapped to a layer-1 logical node, the
    // resulting edge should contain those logical nodes.
    assertThat(
        edge.toLogicalEdge(networkConfigurations),
        equalTo(
            new Layer1Edge(
                edge.getNode1().toLogicalNode(networkConfigurations),
                edge.getNode2().toLogicalNode(networkConfigurations))));
  }
}
