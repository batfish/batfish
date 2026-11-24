package org.batfish.vendor.check_point_management.parsing.serviceother;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Test of {@link EmptyAstNode}. */
final class EmptyAstNodeTest {

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EmptyAstNode.instance()), equalTo(EmptyAstNode.instance()));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EmptyAstNode.instance(), EmptyAstNode.instance())
        .testEquals();
  }
}
