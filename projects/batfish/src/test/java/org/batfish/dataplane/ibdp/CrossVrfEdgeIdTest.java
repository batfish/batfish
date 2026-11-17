package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests of {@link org.batfish.dataplane.ibdp.CrossVrfEdgeId} */
final class CrossVrfEdgeIdTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new CrossVrfEdgeId("vrf", "rib"), new CrossVrfEdgeId("vrf", "rib"))
        .addEqualityGroup(new CrossVrfEdgeId("vrf2", "rib"))
        .addEqualityGroup(new CrossVrfEdgeId("vrf", "rib2"))
        .testEquals();
  }

  @Test
  void testComparator() {
    List<CrossVrfEdgeId> ordered =
        ImmutableList.of(
            new CrossVrfEdgeId("a", "a"),
            new CrossVrfEdgeId("a", "b"),
            new CrossVrfEdgeId("a", "c"),
            new CrossVrfEdgeId("b", "a"));
    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(ordered.get(i).compareTo(ordered.get(j))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }
}
