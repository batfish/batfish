package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link OutgoingAstNode}. */
public final class OutgoingAstNodeTest {

  @Test
  public void testJavaSerialization() {
    OutgoingAstNode obj = new OutgoingAstNode("foo");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    OutgoingAstNode obj = new OutgoingAstNode("foo");
    new EqualsTester()
        .addEqualityGroup(obj, new OutgoingAstNode("foo"))
        .addEqualityGroup(new OutgoingAstNode("bar"))
        .testEquals();
  }
}
