package org.batfish.common.topology.broadcast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class PreserveTest {
  @Test
  void testPreserve() {
    Preserve<Integer> testing = Preserve.get();
    assertThat(testing.traverse(5), equalTo(Optional.of(5)));
    assertThat(testing.traverse(6), equalTo(Optional.of(6)));
  }
}
