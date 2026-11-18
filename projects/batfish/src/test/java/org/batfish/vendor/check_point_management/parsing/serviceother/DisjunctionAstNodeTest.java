package org.batfish.vendor.check_point_management.parsing.serviceother;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link DisjunctionAstNode}. */
final class DisjunctionAstNodeTest {

  @Test
  void testJavaSerialization() {
    DisjunctionAstNode obj = new DisjunctionAstNode();
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  void testEquals() {
    DisjunctionAstNode obj = new DisjunctionAstNode();
    new EqualsTester()
        .addEqualityGroup(obj, new DisjunctionAstNode())
        .addEqualityGroup(new DisjunctionAstNode(TcpAstNode.instance()))
        .testEquals();
  }

  @Test
  void testOr() {
    assertThat(
        new DisjunctionAstNode().or(TcpAstNode.instance()),
        equalTo(new DisjunctionAstNode(TcpAstNode.instance())));
    assertThat(
        TcpAstNode.instance().or(UdpAstNode.instance()),
        equalTo(new DisjunctionAstNode(TcpAstNode.instance(), UdpAstNode.instance())));
  }
}
