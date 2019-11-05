package org.batfish.version;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;
import org.junit.Test;

public class BatfishVersionTest {
  @Test
  public void testBatfishVersionIsLoaded() {
    // Make sure that BatfishVersion is found by the service loader
    assertThat(
        ServiceLoader.load(Versioned.class, ClassLoader.getSystemClassLoader()),
        hasItem(instanceOf(BatfishVersion.class)));
  }
}
