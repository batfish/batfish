package org.batfish.common;

import static org.batfish.common.Version.PROPERTIES_PATH;
import static org.batfish.common.Version.UNKNOWN_VERSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link Version}. */
public class VersionTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void versionExtractionFromProperties() {
    String extractedVersion = Version.getPropertiesVersion(PROPERTIES_PATH, "batfish_version");
    String extractedVersionBadKey = Version.getPropertiesVersion(PROPERTIES_PATH, "bogus");
    String extractedVersionBadPath = Version.getPropertiesVersion("org.bogus", "batfish_version");

    // Confirm version extraction works for a valid properties path and key
    assertThat(extractedVersion, not(containsString("project.version")));
    assertThat(extractedVersion, not(containsString(UNKNOWN_VERSION)));

    // Confirm version extraction returns unknown version for invalid properties path
    assertThat(extractedVersionBadPath, equalTo(UNKNOWN_VERSION));

    // Confirm version extraction returns unknown version for invalid key
    assertThat(extractedVersionBadKey, equalTo(UNKNOWN_VERSION));
  }
}
