package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link TypesFirstAscendingSpaceSeparatedTest}. */
public final class TypesFirstAscendingSpaceSeparatedTest {

  private static final TypesFirstAscendingSpaceSeparated EXPR =
      new TypesFirstAscendingSpaceSeparated(IntegerValueRendering.instance());

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(EXPR, TypesFirstAscendingSpaceSeparated.class), equalTo(EXPR));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            EXPR, EXPR, new TypesFirstAscendingSpaceSeparated(IntegerValueRendering.instance()))
        .addEqualityGroup(new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
