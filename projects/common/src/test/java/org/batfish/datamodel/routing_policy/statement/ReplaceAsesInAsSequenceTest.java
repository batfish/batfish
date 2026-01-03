package org.batfish.datamodel.routing_policy.statement;

import static org.batfish.datamodel.routing_policy.statement.ReplaceAsesInAsSequence.anyAs;
import static org.batfish.datamodel.routing_policy.statement.ReplaceAsesInAsSequence.localAsOrConfedIfNeighborNotInConfed;
import static org.batfish.datamodel.routing_policy.statement.ReplaceAsesInAsSequence.sequenceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ReplaceAsesInAsSequence}. */
@ParametersAreNonnullByDefault
public final class ReplaceAsesInAsSequenceTest {

  @Test
  public void testJavaSerialization() {
    {
      ReplaceAsesInAsSequence obj =
          new ReplaceAsesInAsSequence(
              sequenceOf(ImmutableList.of(1L)), localAsOrConfedIfNeighborNotInConfed());
      assertThat(SerializationUtils.clone(obj), equalTo(obj));
    }
    {
      ReplaceAsesInAsSequence obj =
          new ReplaceAsesInAsSequence(anyAs(), localAsOrConfedIfNeighborNotInConfed());
      assertThat(SerializationUtils.clone(obj), equalTo(obj));
    }
  }

  @Test
  public void testJacksonSerialization() {
    {
      ReplaceAsesInAsSequence obj =
          new ReplaceAsesInAsSequence(
              sequenceOf(ImmutableList.of(1L)), localAsOrConfedIfNeighborNotInConfed());
      assertThat(BatfishObjectMapper.clone(obj, Statement.class), equalTo(obj));
    }
    {
      ReplaceAsesInAsSequence obj =
          new ReplaceAsesInAsSequence(anyAs(), localAsOrConfedIfNeighborNotInConfed());
      assertThat(BatfishObjectMapper.clone(obj, Statement.class), equalTo(obj));
    }
  }

  @Test
  public void testEquals() {
    ReplaceAsesInAsSequence obj =
        new ReplaceAsesInAsSequence(anyAs(), localAsOrConfedIfNeighborNotInConfed());
    new EqualsTester()
        .addEqualityGroup(
            obj, new ReplaceAsesInAsSequence(anyAs(), localAsOrConfedIfNeighborNotInConfed()))
        .addEqualityGroup(
            new ReplaceAsesInAsSequence(
                sequenceOf(ImmutableList.of(1L)), localAsOrConfedIfNeighborNotInConfed()))
        .addEqualityGroup(
            new ReplaceAsesInAsSequence(
                sequenceOf(ImmutableList.of(1L, 2L)), localAsOrConfedIfNeighborNotInConfed()))
        .testEquals();
  }
}
