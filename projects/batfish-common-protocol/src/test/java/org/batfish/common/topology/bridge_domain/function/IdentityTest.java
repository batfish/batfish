package org.batfish.common.topology.bridge_domain.function;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.junit.Test;

public class IdentityTest {
  @Test
  public void testPreserve() {
    Identity<Integer> testing = Identity.get();
    assertThat(testing.traverse(5), equalTo(Optional.of(5)));
    assertThat(testing.traverse(6), equalTo(Optional.of(6)));
  }
}
