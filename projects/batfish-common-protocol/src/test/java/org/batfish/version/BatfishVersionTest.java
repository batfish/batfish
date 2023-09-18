package org.batfish.version;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
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
