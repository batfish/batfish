package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link CommunityMatchRegex}. */
public final class CommunityMatchRegexTest {

  private static final CommunityMatchRegex EXPR =
      new CommunityMatchRegex(IntegerValueRendering.instance(), "a");

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(EXPR, CommunityMatchRegex.class), equalTo(EXPR));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            EXPR, EXPR, new CommunityMatchRegex(IntegerValueRendering.instance(), "a"))
        .addEqualityGroup(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "a"))
        .addEqualityGroup(new CommunityMatchRegex(ColonSeparatedRendering.instance(), "b"))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
