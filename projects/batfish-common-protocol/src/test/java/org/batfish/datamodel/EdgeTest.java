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

  private static Edge EDGE =
      new Edge(new NodeInterfacePair("tail", "tailInt"), new NodeInterfacePair("head", "headInt"));

  @Test
  public void testFactory() {
    assertThat(Edge.of("tail", "tailInt", "head", "headInt"), equalTo(EDGE));
  }

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
            new Edge(
                new NodeInterfacePair("tail", "tailInt"), new NodeInterfacePair("head", "headInt")))
        .addEqualityGroup(EDGE.reverse())
        .addEqualityGroup(
            new Edge(
                new NodeInterfacePair("tail1", "tailInt"),
                new NodeInterfacePair("head", "headInt")))
        .addEqualityGroup(
            new Edge(
                new NodeInterfacePair("tail", "tailint"), new NodeInterfacePair("head", "headInt")))
        .addEqualityGroup(
            new Edge(
                new NodeInterfacePair("tail", "tailInt"), new NodeInterfacePair("hEad", "headInt")))
        .addEqualityGroup(
            new Edge(
                new NodeInterfacePair("tail", "tailInt"),
                new NodeInterfacePair("head", "headIntOther")))
        .testEquals();
  }

  @Test
  public void testReverse() {
    assertThat(
        EDGE.reverse(),
        equalTo(
            new Edge(
                new NodeInterfacePair("head", "headInt"),
                new NodeInterfacePair("tail", "tailInt"))));
  }
}
