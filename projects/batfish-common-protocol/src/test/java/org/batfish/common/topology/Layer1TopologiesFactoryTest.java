package org.batfish.common.topology;

import static org.batfish.common.topology.Layer1Topologies.INVALID_INTERFACE;
import static org.batfish.datamodel.InterfaceType.AGGREGATED;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.NetworkFactory;
import org.junit.Test;

/** Tests of {@link Layer1TopologiesFactory}. */
public final class Layer1TopologiesFactoryTest {
  @Test
  public void testCanonicalization() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().setHostname("c").build();
    nf.interfaceBuilder().setOwner(c).setName("i").setType(PHYSICAL).build();
    nf.interfaceBuilder().setOwner(c).setName("I2").setType(PHYSICAL).build();
    Layer1Topology l1 =
        new Layer1Topology(
            // both c[I] and c[I2] will be canonicalized
            new Layer1Edge("c", "I", "c", "i2"),
            // c[I] canonicalized, but d does not exist.
            new Layer1Edge("c", "I", "d", "iJk"));
    Layer1Topologies topologies =
        Layer1TopologiesFactory.create(l1, Layer1Topology.EMPTY, ImmutableMap.of("c", c));

    assertThat(
        "c[i] and c[I2] are canonicalized, but nonexistent d[iJk] is just lowercased",
        topologies.getUserProvidedL1(),
        equalTo(
            new Layer1Topology(
                new Layer1Edge("c", "i", "c", "I2"), new Layer1Edge("c", "i", "d", "ijk"))));
  }

  @Test
  public void testLogicalTopologyConversion() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().setHostname("c").build();
    nf.interfaceBuilder().setOwner(c).setName("agg").setType(AGGREGATED).build();
    nf.interfaceBuilder().setOwner(c).setName("i").setType(PHYSICAL).setChannelGroup("agg").build();
    nf.interfaceBuilder().setOwner(c).setName("i2").setType(PHYSICAL).build();
    nf.interfaceBuilder()
        .setOwner(c)
        .setName("i3")
        .setType(PHYSICAL)
        .setChannelGroup("noagg")
        .build();
    Layer1Topology l1 =
        new Layer1Topology(
            // c[I] canonicalized then aggregates ; c[I2] canonicalized and preserved
            new Layer1Edge("c", "I", "c", "I2"),
            // c[i] aggregated, d[i] invalid -- but edge preserved
            new Layer1Edge("c", "i", "d", "i"));
    Layer1Topology synthL1 =
        new Layer1Topology( // c[i3] invalid agg, c[i4] invalid -- but the edge stays
            new Layer1Edge("c", "i3", "c", "i4"));
    Layer1Topologies topologies =
        Layer1TopologiesFactory.create(l1, synthL1, ImmutableMap.of("c", c));

    assertThat(
        topologies.getLogicalL1(),
        equalTo(
            new Layer1Topology(
                new Layer1Edge("c", "agg", "c", "i2"),
                new Layer1Edge(
                    "c",
                    "agg",
                    INVALID_INTERFACE.getHostname(),
                    INVALID_INTERFACE.getInterfaceName()),
                new Layer1Edge(
                    INVALID_INTERFACE.getHostname(),
                    INVALID_INTERFACE.getInterfaceName(),
                    INVALID_INTERFACE.getHostname(),
                    INVALID_INTERFACE.getInterfaceName()))));
    assertThat(
        topologies.getActiveLogicalL1(),
        equalTo(
            new Layer1Topology(
                new Layer1Edge("c", "agg", "c", "i2"), new Layer1Edge("c", "i2", "c", "agg"))));
  }
}
