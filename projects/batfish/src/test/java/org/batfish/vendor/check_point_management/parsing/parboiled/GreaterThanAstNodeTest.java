package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link GreaterThanAstNode}. */
public final class GreaterThanAstNodeTest {

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(GreaterThanAstNode.instance()),
        equalTo(GreaterThanAstNode.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(GreaterThanAstNode.instance(), GreaterThanAstNode.instance())
        .testEquals();
  }
}
