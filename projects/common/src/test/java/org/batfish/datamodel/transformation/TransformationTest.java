package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.transformation.Noop.NOOP_SOURCE_NAT;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.shiftDestinationIp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests for {@link Transformation}. */
public class TransformationTest {
  @Test
  public void testEquals() {
    Transformation trivial = always().apply(NOOP_SOURCE_NAT).build();
    new EqualsTester()
        .addEqualityGroup(trivial, always().apply(NOOP_SOURCE_NAT).build())
        .addEqualityGroup(when(FALSE).apply(NOOP_SOURCE_NAT).build())
        .addEqualityGroup(always().apply(shiftDestinationIp(Prefix.ZERO)).build())
        .addEqualityGroup(always().apply(NOOP_SOURCE_NAT).setAndThen(trivial).build())
        .addEqualityGroup(always().apply(NOOP_SOURCE_NAT).setOrElse(trivial).build())
        .testEquals();
  }

  @Test
  public void testSerialization() {
    Transformation t =
        when(FALSE)
            .apply(shiftDestinationIp(Prefix.ZERO))
            .setAndThen(always().apply(NOOP_SOURCE_NAT).build())
            .setOrElse(always().apply(NOOP_SOURCE_NAT).build())
            .build();
    assertEquals(t, SerializationUtils.clone(t));
  }

  /**
   * A NAT rule-set is converted into a chain of {@link Transformation}s linked by {@code orElse}
   * (see {@code NatRuleSet#rulesTransformation}). A rule-set with thousands of rules produces a
   * chain thousands of links deep. Default Java serialization recurses once per link, so such a
   * chain must round-trip without a {@link StackOverflowError}.
   */
  @Test
  public void testSerializationDeepOrElseChain() {
    int depth = 100_000;
    Transformation t = null;
    for (int i = 0; i < depth; i++) {
      t = when(FALSE).apply(NOOP_SOURCE_NAT).setOrElse(t).build();
    }

    Transformation cloned = SerializationUtils.clone(t);

    // Walk both orElse spines iteratively (recursive equals would itself overflow) and confirm the
    // chain survived the round-trip intact.
    Transformation original = t;
    int count = 0;
    while (original != null) {
      assertEquals(original.getGuard(), cloned.getGuard());
      assertEquals(original.getTransformationSteps(), cloned.getTransformationSteps());
      original = original.getOrElse();
      cloned = cloned.getOrElse();
      count++;
    }
    assertEquals(depth, count);
    assertNull(cloned);
  }
}
