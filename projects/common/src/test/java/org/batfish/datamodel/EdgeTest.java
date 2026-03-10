package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link Edge} */
@RunWith(JUnit4.class)
public class EdgeTest {

  private static final Edge EDGE =
      new Edge(NodeInterfacePair.of("tail", "tailInt"), NodeInterfacePair.of("head", "headInt"));

  @Test
  public void testGetterEquivalence() {
    assertThat(EDGE.getNode1(), equalTo(EDGE.getTail().getHostname()));
    assertThat(EDGE.getNode2(), equalTo(EDGE.getHead().getHostname()));
    assertThat(EDGE.getInt1(), equalTo(EDGE.getTail().getInterface()));
    assertThat(EDGE.getInt2(), equalTo(EDGE.getHead().getInterface()));
  }

  @Test
  public void testToString() {
    assertThat(EDGE.toString(), equalTo("<tail:tailInt, head:headInt>"));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            EDGE,
            Edge.of(EDGE.getNode1(), EDGE.getInt1(), EDGE.getNode2(), EDGE.getInt2()),
            new Edge(EDGE.getTail(), EDGE.getHead()))
        .addEqualityGroup(EDGE.reverse(), new Edge(EDGE.getHead(), EDGE.getTail()))
        .addEqualityGroup(
            Edge.of(EDGE.getNode1() + '1', EDGE.getInt1(), EDGE.getNode2(), EDGE.getInt2()))
        .addEqualityGroup(
            Edge.of(EDGE.getNode1(), EDGE.getInt1() + '1', EDGE.getNode2(), EDGE.getInt2()))
        .addEqualityGroup(
            Edge.of(EDGE.getNode1(), EDGE.getInt1(), EDGE.getNode2() + '1', EDGE.getInt2()))
        .addEqualityGroup(
            Edge.of(EDGE.getNode1(), EDGE.getInt1(), EDGE.getNode2(), EDGE.getInt2() + '1'))
        .testEquals();
  }

  @Test
  public void testReverse() {
    assertThat(
        EDGE.reverse(),
        equalTo(
            new Edge(
                NodeInterfacePair.of("head", "headInt"), NodeInterfacePair.of("tail", "tailInt"))));
  }
}
