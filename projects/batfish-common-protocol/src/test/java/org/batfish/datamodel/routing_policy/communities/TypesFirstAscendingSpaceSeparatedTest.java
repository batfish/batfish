package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link TypesFirstAscendingSpaceSeparatedTest}. */
public final class TypesFirstAscendingSpaceSeparatedTest {

  private static final TypesFirstAscendingSpaceSeparated EXPR =
      new TypesFirstAscendingSpaceSeparated(IntegerValueRendering.instance());

  @Test
  public void testJacksonSerialization() throws IOException {
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
