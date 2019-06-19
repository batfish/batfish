package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.pojo.Aggregate.AggregateType;
import org.junit.Test;

/** Tests of {@link Topology}. */
public class TopologyTest {

  @Test
  public void testJsonSerialization() throws IOException {
    Node node = new Node("node");
    Link link = new Link("src", "dst");
    Interface iface = new Interface(node.getId(), "iface");
    Aggregate a = new Aggregate("cloud", AggregateType.CLOUD);
    a.setContents(ImmutableSet.of(node.getId()));

    Topology t = new Topology("testrig");
    t.setNodes(ImmutableSet.of(node));
    t.setInterfaces(ImmutableSet.of(iface));
    t.setLinks(ImmutableSet.of(link));
    t.setAggregates(ImmutableSet.of(a));

    Topology topo = BatfishObjectMapper.clone(t, Topology.class);

    assertThat(topo.getId(), equalTo(Topology.getId("testrig")));
    assertThat(topo.getTestrigName(), equalTo("testrig"));
    assertThat(topo.getAggregates().size(), equalTo(1));
    assertThat(
        topo.getOrCreateAggregate("cloud", AggregateType.CLOUD).getContents().size(), equalTo(1));
    assertThat(topo.getInterfaces().size(), equalTo(1));
    assertThat(topo.getLinks().size(), equalTo(1));
    assertThat(topo.getNodes().size(), equalTo(1));
  }
}
