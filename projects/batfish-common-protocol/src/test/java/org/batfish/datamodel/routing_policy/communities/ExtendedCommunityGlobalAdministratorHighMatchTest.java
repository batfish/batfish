package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link ExtendedCommunityGlobalAdministratorHighMatch}. */
public final class ExtendedCommunityGlobalAdministratorHighMatchTest {

  private static final ExtendedCommunityGlobalAdministratorHighMatch OBJ =
      new ExtendedCommunityGlobalAdministratorHighMatch(
          new IntComparison(IntComparator.EQ, new LiteralInt(1)));

  @Test
  public void testJacksonSerialization() throws IOException {
    assertThat(
        BatfishObjectMapper.clone(OBJ, ExtendedCommunityGlobalAdministratorHighMatch.class),
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
            new ExtendedCommunityGlobalAdministratorHighMatch(
                new IntComparison(IntComparator.EQ, new LiteralInt(1))))
        .addEqualityGroup(
            new ExtendedCommunityGlobalAdministratorHighMatch(
                new IntComparison(IntComparator.EQ, new LiteralInt(2))))
        .testEquals();
  }
}
