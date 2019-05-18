package org.batfish.datamodel.collections;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.RipNeighbor;
import org.junit.Test;

public class VerboseRipEdgeTest {
  @Test
  public void testCompareTo() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    String node1 = "node1";
    String node2 = "node2";
    IpEdge ipEdge1 = new IpEdge(node1, ip1, node1, ip1);
    IpEdge ipEdge2 = new IpEdge(node2, ip2, node2, ip2);
    RipNeighbor neighbor1 = new RipNeighbor(ip1, ip1);
    RipNeighbor neighbor2 = new RipNeighbor(ip2, ip2);
    VerboseRipEdge ripEdge1 = new VerboseRipEdge(neighbor1, neighbor1, ipEdge1);
    VerboseRipEdge ripEdge1Copy = new VerboseRipEdge(neighbor1, neighbor1, ipEdge1);
    VerboseRipEdge ripEdge2 = new VerboseRipEdge(neighbor2, neighbor2, ipEdge2);
    assertThat(ripEdge1.compareTo(ripEdge1Copy), is(0));
    assertThat(ripEdge1.compareTo(ripEdge2), lessThan(0));
    assertThat(ripEdge2.compareTo(ripEdge1), greaterThan(0));
  }
}
