package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link StandardCommunityHighMatch}. */
public final class StandardCommunityHighMatchTest {

  private static final StandardCommunityHighMatch OBJ =
      new StandardCommunityHighMatch(new IntComparison(IntComparator.EQ, new LiteralInt(1)));

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(OBJ, StandardCommunityHighMatch.class), equalTo(OBJ));
  }

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(OBJ), equalTo(OBJ));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            OBJ,
            OBJ,
            new StandardCommunityHighMatch(new IntComparison(IntComparator.EQ, new LiteralInt(1))))
        .addEqualityGroup(
            new StandardCommunityHighMatch(new IntComparison(IntComparator.GT, new LiteralInt(1))))
        .testEquals();
  }
}
