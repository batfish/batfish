package org.batfish.vendor.check_point_management.parsing.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link UdpAstNode}. */
public final class UdpAstNodeTest {

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(UdpAstNode.instance()), equalTo(UdpAstNode.instance()));
  }

  @Test
  public void testEquals() {
    new EqualsTester().addEqualityGroup(UdpAstNode.instance(), UdpAstNode.instance()).testEquals();
  }
}
