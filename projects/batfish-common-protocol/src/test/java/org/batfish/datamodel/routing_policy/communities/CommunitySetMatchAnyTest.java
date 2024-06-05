package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link CommunitySetMatchAny}. */
public final class CommunitySetMatchAnyTest {

  private static final CommunitySetMatchAny EXPR = new CommunitySetMatchAny(ImmutableList.of());

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(EXPR, CommunitySetMatchAny.class), equalTo(EXPR));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(EXPR), equalTo(EXPR));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EXPR, EXPR, new CommunitySetMatchAny(ImmutableList.of()))
        .addEqualityGroup(
            new CommunitySetMatchAny(
                ImmutableList.of(new HasCommunity(AllStandardCommunities.instance()))))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
