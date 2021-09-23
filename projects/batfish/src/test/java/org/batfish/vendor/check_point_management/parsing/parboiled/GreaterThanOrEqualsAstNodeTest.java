package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link GreaterThanOrEqualsAstNode}. */
public final class GreaterThanOrEqualsAstNodeTest {

  @Test
  public void testJavaSerialization() {
    assertThat(
        SerializationUtils.clone(GreaterThanOrEqualsAstNode.instance()),
        equalTo(GreaterThanOrEqualsAstNode.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            GreaterThanOrEqualsAstNode.instance(), GreaterThanOrEqualsAstNode.instance())
        .testEquals();
  }
}
