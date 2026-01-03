package org.batfish.datamodel.routing_policy.as_path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link AsSetsMatchingRanges}. */
public final class AsSetsMatchingRangesTest {

  @Test
  public void testJavaSerialization() {
    AsSetsMatchingRanges obj = AsSetsMatchingRanges.of(false, false, ImmutableList.of());
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    AsSetsMatchingRanges obj = AsSetsMatchingRanges.of(false, false, ImmutableList.of());
    assertThat(BatfishObjectMapper.clone(obj, AsSetsMatchingRanges.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    AsSetsMatchingRanges obj = AsSetsMatchingRanges.of(false, false, ImmutableList.of());
    new EqualsTester()
        .addEqualityGroup(obj, AsSetsMatchingRanges.of(false, false, ImmutableList.of()))
        .addEqualityGroup(AsSetsMatchingRanges.of(true, false, ImmutableList.of()))
        .addEqualityGroup(AsSetsMatchingRanges.of(false, true, ImmutableList.of()))
        .addEqualityGroup(
            AsSetsMatchingRanges.of(false, false, ImmutableList.of(Range.singleton(1L))))
        .testEquals();
  }
}
