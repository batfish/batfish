package org.batfish.common.topology;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
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

public final class Layer1NodeTest {
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
  public void testToLogicalNodeAggregate() {
    String c1Name = "c1";
    String iName = "i1";
    String aName = "a1";

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(iName).build().setChannelGroup(aName);
    _ib.setName(aName).build();

    NetworkConfigurations networkConfigurations =
        NetworkConfigurations.of(ImmutableSortedMap.of(c1Name, c1));
    Layer1Node node = new Layer1Node(c1Name, iName);

    // If node is member of an aggregate, the resulting logical node should be for the aggregate
    // interface.
    assertThat(node.toLogicalNode(networkConfigurations), equalTo(new Layer1Node(c1Name, aName)));
  }

  @Test
  public void testToLogicalNodeInactiveAggregate() {
    String c1Name = "c1";
    String iName = "i1";
    String aName = "a1";

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(iName).build().setChannelGroup(aName);
    _ib.setName(aName).setActive(false).build();

    NetworkConfigurations networkConfigurations =
        NetworkConfigurations.of(ImmutableSortedMap.of(c1Name, c1));
    Layer1Node node = new Layer1Node(c1Name, iName);

    // If node is member of a disabled aggregate, the result should be null
    assertThat(node.toLogicalNode(networkConfigurations), nullValue());
  }

  @Test
  public void testToLogicalNodeMissingAggregate() {
    String c1Name = "c1";
    String iName = "i1";

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(iName).build().setChannelGroup("missing");

    NetworkConfigurations networkConfigurations =
        NetworkConfigurations.of(ImmutableSortedMap.of(c1Name, c1));
    Layer1Node node = new Layer1Node(c1Name, iName);

    // If node is member of a missing aggregate, the result should be null
    assertThat(node.toLogicalNode(networkConfigurations), nullValue());
  }

  @Test
  public void testToLogicalNodeNonAggregate() {
    String c1Name = "c1";
    String iName = "i1";

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(iName).build();

    NetworkConfigurations networkConfigurations =
        NetworkConfigurations.of(ImmutableSortedMap.of(c1Name, c1));
    Layer1Node node = new Layer1Node(c1Name, iName);

    // If node is not member of an aggregate, the resulting logical node should reference the
    // physical interface name.
    assertThat(node.toLogicalNode(networkConfigurations), equalTo(node));
  }

  @Test
  public void testHostnameIsCanonicalized() {
    Layer1Node node = new Layer1Node("NODE", "iface");
    assertThat(node.getHostname(), equalTo("node"));

    // Should be equivalent to a node initialized with lowercase hostname
    assertThat(node, equalTo(new Layer1Node("node", "iface")));

    // Interface names should be case sensitive
    assertThat(node, not(equalTo(new Layer1Node("node", "IFACE"))));
  }
}
