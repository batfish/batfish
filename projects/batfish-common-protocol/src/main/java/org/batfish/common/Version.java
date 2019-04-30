package org.batfish.common;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nullable;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * A utility class to determine the version of Batfish being used and check compatibility between
 * different endpoints across API calls.
 */
public final class Version {
  private static final String PROPERTIES_PATH = "org/batfish/common/common.properties";

  static final String UNKNOWN_VERSION = "0.0.0";
  /**
   * A special version string that is incompatible with all other version. Mainly used for testing.
   */
  public static final String INCOMPATIBLE_VERSION = "x.x.x";

  /**
   * Checks whether the supplied version for another endpoint is compatible with the Batfish version
   * of this process, and throws an error if not.
   *
   * @see #checkCompatibleVersion(String, String, String)
   */
  public static void checkCompatibleVersion(
      String myName, String otherName, @Nullable String otherVersion) {
    checkCompatibleVersion(myName, getVersion(), otherName, otherVersion);
  }

  // Visible for testing.
  static void checkCompatibleVersion(
      String myName, String myVersion, String otherName, @Nullable String otherVersion) {
    checkArgument(
        isCompatibleVersion(myName, myVersion, otherName, otherVersion),
        "%s version: '%s' is not compatible with %s version: '%s'",
        otherName,
        otherVersion,
        myName,
        myVersion);
  }

  /**
   * Returns the version of the current build of Batfish, or {@link #UNKNOWN_VERSION} if the version
   * could not be detected.
   */
  public static String getVersion() {
    try {
      Configuration config = new Configurations().properties(PROPERTIES_PATH);
      String version = config.getString("batfish_version");
      if (version.contains("project.version")) {
        // For whatever reason, resource filtering didn't work.
        return UNKNOWN_VERSION;
      }
      return version;
    } catch (Exception e) {
      return UNKNOWN_VERSION;
    }
  }

  /**
   * Returns the version of the current build of Z3, or {@link #UNKNOWN_VERSION} if the version
   * could not be detected.
   */
  public static String getZ3Version() {
    try {
      return com.microsoft.z3.Version.getString();
    } catch (Throwable e) {
      return "unknown, unable to load library";
    }
  }

  /** Returns string indicating the current build of Batfish and Z3. */
  public static String getCompleteVersionString() {
    return String.format("Batfish version: %s\nZ3 version: %s\n", getVersion(), getZ3Version());
  }

  /**
   * Returns {@code true} if the given version of some other endpoint is compatible with the Batfish
   * version of this process.
   *
   * <p>At the time of writing, compatibility is determined on having identical major and minor
   * versions.
   */
  public static boolean isCompatibleVersion(
      String myName, String otherName, @Nullable String otherVersion) {
    return isCompatibleVersion(myName, getVersion(), otherName, otherVersion);
  }

  // Visible for testing.
  static boolean isCompatibleVersion(
      String myName, String myVersion, String otherName, @Nullable String otherVersion) {
    String effectiveOtherVersion = firstNonNull(otherVersion, UNKNOWN_VERSION);

    if (effectiveOtherVersion.equals(INCOMPATIBLE_VERSION)
        || myVersion.equals(INCOMPATIBLE_VERSION)) {
      return false;
    }

    if (effectiveOtherVersion.equals(UNKNOWN_VERSION) || myVersion.equals(UNKNOWN_VERSION)) {
      // Either version is unknown, assume compatible.
      return true;
    }

    DefaultArtifactVersion myArtifactVersion = parseVersion(myVersion, myName);
    DefaultArtifactVersion otherArtifactVersion = parseVersion(effectiveOtherVersion, otherName);
    return myArtifactVersion.getMajorVersion() == otherArtifactVersion.getMajorVersion()
        && myArtifactVersion.getMinorVersion() == otherArtifactVersion.getMinorVersion();
  }

  /** Utility to parse versions, with error handling. */
  private static DefaultArtifactVersion parseVersion(String version, String name) {
    DefaultArtifactVersion artifactVersion = new DefaultArtifactVersion(version);
    if (artifactVersion.getMajorVersion() == 0 && artifactVersion.getMinorVersion() == 0) {
      throw new BatfishException(String.format("Illegal version '%s' for %s", version, name));
    }
    return artifactVersion;
  }

  // Suppress instantiation of utility class.
  private Version() {}
}
