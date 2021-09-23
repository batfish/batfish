package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link LessThanOrEqualsAstNode}. */
public final class LessThanOrEqualsAstNodeTest {

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(LessThanOrEqualsAstNode.instance()),
        equalTo(LessThanOrEqualsAstNode.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(LessThanOrEqualsAstNode.instance(), LessThanOrEqualsAstNode.instance())
        .testEquals();
  }
}
