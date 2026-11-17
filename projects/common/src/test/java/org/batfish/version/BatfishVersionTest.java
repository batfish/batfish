package org.batfish.version;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;

import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class BatfishVersionTest {
  @Test
  void testBatfishVersionIsLoaded() {
    // Make sure that BatfishVersion is found by the service loader
    assertThat(
        ServiceLoader.load(Versioned.class, ClassLoader.getSystemClassLoader()),
        hasItem(instanceOf(BatfishVersion.class)));
  }
}
