package org.batfish.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

/** Tests of {@link Pair} */
public class PairTest {

  private static class PairImpl extends Pair<Integer, Integer> {
    private static final long serialVersionUID = 1L;

    PairImpl(Integer i1, Integer i2) {
      super(i1, i2);
    }
  }

  @Test
  public void testCompareToWithNullSecond() {
    List<PairImpl> ordered =
        ImmutableList.of(
            new PairImpl(1, null), new PairImpl(1, 2), new PairImpl(2, null), new PairImpl(2, 2));
    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(ordered.get(i).compareTo(ordered.get(j))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }
}
