package org.batfish.datamodel.packet_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests of {@link Conjunction} */
class ConjunctionTest {
  @Test
  void testEquals() {
    BoolExpr c = Conjunction.of(TrueExpr.instance());
    new EqualsTester()
        .addEqualityGroup(c, c, Conjunction.of(TrueExpr.instance()))
        .addEqualityGroup(Conjunction.of(FalseExpr.instance()))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    BoolExpr c = Conjunction.of(TrueExpr.instance());
    assertThat(SerializationUtils.clone(c), equalTo(c));
  }

  @Test
  void testJsonSerialization() {
    BoolExpr c = Conjunction.of(TrueExpr.instance());
    assertThat(BatfishObjectMapper.clone(c, Conjunction.class), equalTo(c));
  }
}
