package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link CommunitySetUnion}. */
public final class CommunitySetUnionTest {

  private static final CommunitySetUnion OBJ =
      CommunitySetUnion.of(new CommunitySetExprReference("a"), new CommunitySetExprReference("a"));

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, CommunitySetUnion.class), equalTo(OBJ));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            OBJ,
            OBJ,
            CommunitySetUnion.of(
                new CommunitySetExprReference("a"), new CommunitySetExprReference("a")))
        .addEqualityGroup(
            CommunitySetUnion.of(
                new CommunitySetExprReference("b"), new CommunitySetExprReference("a")))
        .addEqualityGroup(
            CommunitySetUnion.of(
                new CommunitySetExprReference("b"), new CommunitySetExprReference("b")))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
