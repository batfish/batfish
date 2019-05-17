package org.batfish.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

/** Tests of {@link Pair} */
public class PairTest {
  @Test
  public void testCompareToWithNullSecond() {
    List<Pair<Integer, Integer>> ordered =
        ImmutableList.of(
            new Pair<>(1, null), new Pair<>(1, 2), new Pair<>(2, null), new Pair<>(2, 2));
    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(ordered.get(i).compareTo(ordered.get(j))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }
}
