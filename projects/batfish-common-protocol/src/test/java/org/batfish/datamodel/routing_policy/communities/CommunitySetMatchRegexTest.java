package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link CommunitySetMatchRegex}. */
public final class CommunitySetMatchRegexTest {

  private static final CommunitySetMatchRegex EXPR =
      new CommunitySetMatchRegex(
          new TypesFirstAscendingSpaceSeparated(IntegerValueRendering.instance()), "a");

  @Test
  public void testJacksonSerialization() throws IOException {
    assertThat(BatfishObjectMapper.clone(EXPR, CommunitySetMatchRegex.class), equalTo(EXPR));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            EXPR,
            EXPR,
            new CommunitySetMatchRegex(
                new TypesFirstAscendingSpaceSeparated(IntegerValueRendering.instance()), "a"))
        .addEqualityGroup(
            new CommunitySetMatchRegex(
                new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()), "a"))
        .addEqualityGroup(
            new CommunitySetMatchRegex(
                new TypesFirstAscendingSpaceSeparated(ColonSeparatedRendering.instance()), "b"))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
