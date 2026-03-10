package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link CommunityExprsSet}. */
public final class CommunityExprsSetTest {

  private static final CommunityExprsSet OBJ =
      CommunityExprsSet.of(new StandardCommunityHighLowExprs(new LiteralInt(1), new LiteralInt(1)));

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, CommunityExprsSet.class), equalTo(OBJ));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  public void testEquals() {
    CommunityExprsSet obj = new CommunityExprsSet(ImmutableSet.of());
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            obj, obj, new CommunityExprsSet(ImmutableSet.of()), CommunityExprsSet.of())
        .addEqualityGroup(OBJ)
        .testEquals();
  }
}
