package org.batfish.common;

import static org.batfish.common.Version.UNKNOWN_VERSION;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link Version}. */
public class VersionTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void checkCompatibleVersion() {
    Version.checkCompatibleVersion("foo", "1.2.3", "other", "1.2.3");
    Version.checkCompatibleVersion("foo", "1.2.3", "other", "1.2.4");
    Version.checkCompatibleVersion("foo", UNKNOWN_VERSION, "other", "1.2.4");
    Version.checkCompatibleVersion("foo", "1.2.3", "other", UNKNOWN_VERSION);
    Version.checkCompatibleVersion("foo", "1.2.3", "other", "1.2.3-SNAPSHOT");
  }

  @Test
  public void checkIncompatibleVersion() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("other version: '1.3.3' is not compatible with foo version: '1.2.3'");
    Version.checkCompatibleVersion("foo", "1.2.3", "other", "1.3.3");
  }

  @Test
  public void illegalVersions() {
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Illegal version 'zebra' for yours");
    Version.isCompatibleVersion("my", "1.2.3", "yours", "zebra");
  }

  @Test
  public void isCompatibleVersion() {
    // identical versions
    assertTrue(Version.isCompatibleVersion("foo", "1.2.3", "other", "1.2.3"));
    // compatible versions, different patch version
    assertTrue(Version.isCompatibleVersion("foo", "1.2.3", "other", "1.2.4"));
    // mine unknown
    assertTrue(Version.isCompatibleVersion("foo", UNKNOWN_VERSION, "other", "1.2.4"));
    // other unknown
    assertTrue(Version.isCompatibleVersion("foo", "1.2.3", "other", UNKNOWN_VERSION));
    // SNAPSHOT
    assertTrue(Version.isCompatibleVersion("foo", "1.2.3", "other", "1.2.3-SNAPSHOT"));

    // Other newer minor
    assertFalse(Version.isCompatibleVersion("foo", "1.2.3", "other", "1.3.3"));
    // Other newer major
    assertFalse(Version.isCompatibleVersion("foo", "1.2.3", "other", "2.2.3"));
    // Other older minor
    assertFalse(Version.isCompatibleVersion("foo", "1.2.3", "other", "1.1.3"));
    // Other older major
    assertFalse(Version.isCompatibleVersion("foo", "1.2.3", "other", "0.2.3"));
  }

  @Test
  public void versionIsRealAtRuntime() {
    assertThat(Version.getVersion(), not(containsString("project.version")));
    assertThat(Version.getVersion(), not(equalTo(UNKNOWN_VERSION)));
  }
}
