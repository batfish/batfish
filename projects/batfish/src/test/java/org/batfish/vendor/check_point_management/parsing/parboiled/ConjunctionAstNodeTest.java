package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Test of {@link ConjunctionAstNode}. */
public final class ConjunctionAstNodeTest {

  @Test
  public void testEquals() {
    ConjunctionAstNode obj = new ConjunctionAstNode();
    new EqualsTester()
        .addEqualityGroup(obj, new ConjunctionAstNode())
        .addEqualityGroup(new ConjunctionAstNode(TcpAstNode.instance()))
        .testEquals();
  }

  @Test
  public void testAnd() {
    assertThat(
        new ConjunctionAstNode().and(TcpAstNode.instance()),
        equalTo(new ConjunctionAstNode(TcpAstNode.instance())));
    assertThat(
        TcpAstNode.instance().and(UdpAstNode.instance()),
        equalTo(new ConjunctionAstNode(TcpAstNode.instance(), UdpAstNode.instance())));
  }
}
