package org.batfish.datamodel.ospf;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link OspfEdge}. */
public class OspfEdgeTest {
  @Test
  public void testEquals() {
    OspfNode node1 = new OspfNode("a", "i", new Ip("1.2.3.4"));
    OspfNode node2 = new OspfNode("b", "i", new Ip("1.2.3.5"));
    OspfNode node3 = new OspfNode("c", "i", new Ip("1.2.3.6"));
    new EqualsTester()
        .addEqualityGroup(new OspfEdge(node1, node2), new OspfEdge(node1, node2))
        .addEqualityGroup(new OspfEdge(node1, node3))
        .addEqualityGroup(new OspfEdge(node2, node3))
        .addEqualityGroup(new OspfEdge(node3, node2))
        .addEqualityGroup(new OspfEdge(node3, node1), new OspfEdge(node1, node3).reverse())
        .addEqualityGroup(new OspfEdge(node1, node1))
        .testEquals();
  }

  @Test
  public void testComparison() {
    OspfNode node1 = new OspfNode("a", "i", new Ip("1.2.3.4"));
    OspfNode node2 = new OspfNode("b", "i", new Ip("1.2.3.5"));
    OspfNode node3 = new OspfNode("c", "i", new Ip("1.2.3.6"));
    List<OspfEdge> expected =
        ImmutableList.of(
            new OspfEdge(node1, node1),
            new OspfEdge(node1, node2),
            new OspfEdge(node1, node3),
            new OspfEdge(node2, node1),
            new OspfEdge(node2, node2),
            new OspfEdge(node2, node3),
            new OspfEdge(node3, node1),
            new OspfEdge(node3, node2),
            new OspfEdge(node3, node3));
    assertThat(Ordering.natural().sortedCopy(expected), equalTo(expected));
    assertThat(Ordering.natural().sortedCopy(Lists.reverse(expected)), equalTo(expected));
  }

  @Test
  public void testReverse() {
    OspfNode node1 = new OspfNode("a", "i", new Ip("1.2.3.4"));
    OspfNode node2 = new OspfNode("b", "i", new Ip("1.2.3.5"));
    OspfEdge rev = new OspfEdge(node1, node2).reverse();
    assertThat(rev.getNode1(), is(node2));
    assertThat(rev.getNode2(), is(node1));
  }

  @Test
  public void testSerialization() throws IOException {
    OspfEdge edge = new OspfEdge(new OspfNode("a", "i", Ip.ZERO), new OspfNode("b", "j", Ip.MAX));
    assertThat(BatfishObjectMapper.clone(edge, OspfEdge.class), equalTo(edge));
  }
}
