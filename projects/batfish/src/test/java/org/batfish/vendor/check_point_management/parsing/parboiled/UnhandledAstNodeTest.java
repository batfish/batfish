package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link UnhandledAstNode}. */
public final class UnhandledAstNodeTest {

  @Test
  public void testJavaSerialization() {
    UnhandledAstNode obj = UnhandledAstNode.of("foo");
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    UnhandledAstNode obj = UnhandledAstNode.of("foo");
    new EqualsTester()
        .addEqualityGroup(obj, UnhandledAstNode.of("foo"))
        .addEqualityGroup(UnhandledAstNode.of("bar"))
        .testEquals();
  }
}
