package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link FalseExpr} */
class FalseExprTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(FalseExpr.instance(), FalseExpr.instance())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(FalseExpr.instance()), equalTo(FalseExpr.instance()));
  }

  @Test
  void testJsonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(FalseExpr.instance(), FalseExpr.class),
        equalTo(FalseExpr.instance()));
  }
}
