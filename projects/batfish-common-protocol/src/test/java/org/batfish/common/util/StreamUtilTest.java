package org.batfish.common.util;

import static org.batfish.common.util.StreamUtil.toListInRandomOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StreamUtilTest {
  @Test
  public void testToListInRandomOrder() {
    int num = 10;
    Random r = new Random(5);
    List<Integer> ints = IntStream.range(0, num).boxed().collect(Collectors.toList());
    List<Integer> afterwards = toListInRandomOrder(IntStream.range(0, num).boxed(), r);
    assertThat(afterwards, not(equalTo(ints))); // Guaranteed given fixed seed.
    assertThat(ImmutableSet.copyOf(afterwards), equalTo(ImmutableSet.copyOf(ints)));

    // Without fixed seed, just check that the sets are the same.
    List<Integer> afterwardsRandomSeed = toListInRandomOrder(IntStream.range(0, num).boxed());
    assertThat(ImmutableSet.copyOf(afterwardsRandomSeed), equalTo(ImmutableSet.copyOf(ints)));
  }
}
