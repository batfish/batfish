package org.batfish.version;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.util.ServiceLoader;
import org.junit.Test;

/** Test of {@link WorkMgrV2ApiVersion}. */
public final class WorkMgrV2ApiVersionTest {
  @Test
  public void testWorkMgrV2ApiVersionIsLoaded() {
    // Make sure that WorkMgrV2ApiVersion is found by the service loader
    assertThat(
        ServiceLoader.load(Versioned.class, ClassLoader.getSystemClassLoader()),
        hasItem(instanceOf(WorkMgrV2ApiVersion.class)));
  }

  @Test
  public void testMajorVersion() {
    assertThat(WorkMgrV2ApiVersion.getVersionStatic(), startsWith("2."));
  }
}
