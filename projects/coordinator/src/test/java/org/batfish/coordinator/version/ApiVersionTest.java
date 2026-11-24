package org.batfish.coordinator.version;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.matchesRegex;

import java.util.ServiceLoader;
import java.util.regex.Pattern;
import org.batfish.version.Versioned;
import org.junit.jupiter.api.Test;

/** Test of {@link ApiVersion}. */
final class ApiVersionTest {
  @Test
  void testApiVersionIsLoaded() {
    // Make sure that ApiVersion is found by the service loader
    assertThat(
        ServiceLoader.load(Versioned.class, ClassLoader.getSystemClassLoader()),
        hasItem(instanceOf(ApiVersion.class)));
  }

  @Test
  void testMajorVersion() {
    assertThat(ApiVersion.getVersionStatic(), matchesRegex(SEMVER));
  }

  private static final Pattern SEMVER = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
}
