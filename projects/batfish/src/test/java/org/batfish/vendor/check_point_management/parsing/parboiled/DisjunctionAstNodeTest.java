package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link DisjunctionAstNode}. */
public final class DisjunctionAstNodeTest {

  @Test
  public void testJavaSerialization() {
    DisjunctionAstNode obj = new DisjunctionAstNode();
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    DisjunctionAstNode obj = new DisjunctionAstNode();
    new EqualsTester()
        .addEqualityGroup(obj, new DisjunctionAstNode())
        .addEqualityGroup(new DisjunctionAstNode(TcpAstNode.instance()))
        .testEquals();
  }

  @Test
  public void testOr() {
    assertThat(
        new DisjunctionAstNode().or(TcpAstNode.instance()),
        equalTo(new DisjunctionAstNode(TcpAstNode.instance())));
    assertThat(
        TcpAstNode.instance().or(UdpAstNode.instance()),
        equalTo(new DisjunctionAstNode(TcpAstNode.instance(), UdpAstNode.instance())));
  }
}
