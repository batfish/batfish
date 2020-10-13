package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LongComparison;
import org.junit.Test;

/** Test of {@link ExtendedCommunityGlobalAdministratorMatch}. */
public final class ExtendedCommunityGlobalAdministratorMatchTest {

  private static final ExtendedCommunityGlobalAdministratorMatch OBJ =
      new ExtendedCommunityGlobalAdministratorMatch(
          new LongComparison(IntComparator.EQ, new LiteralLong(1L)));

  @Test
  public void testJacksonSerialization() {
    assertThat(
        BatfishObjectMapper.clone(OBJ, ExtendedCommunityGlobalAdministratorMatch.class),
        equalTo(OBJ));
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
            new ExtendedCommunityGlobalAdministratorMatch(
                new LongComparison(IntComparator.EQ, new LiteralLong(1L))))
        .addEqualityGroup(
            new ExtendedCommunityGlobalAdministratorMatch(
                new LongComparison(IntComparator.EQ, new LiteralLong(2L))))
        .testEquals();
  }
}
