package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link IncomingAstNode}. */
public final class IncomingAstNodeTest {

  @Test
  public void testJavaSerialization() {
    IncomingAstNode obj = new IncomingAstNode("foo");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    IncomingAstNode obj = new IncomingAstNode("foo");
    new EqualsTester()
        .addEqualityGroup(obj, new IncomingAstNode("foo"))
        .addEqualityGroup(new IncomingAstNode("bar"))
        .testEquals();
  }
}
