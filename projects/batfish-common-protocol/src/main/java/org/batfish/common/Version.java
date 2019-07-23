package org.batfish.common;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

/** A utility class to help with extracting version information. */
public final class Version {
  @VisibleForTesting static final String PROPERTIES_PATH = "org/batfish/common/common.properties";

  public static final String UNKNOWN_VERSION = "0.0.0";
  /**
   * Returns the version of the current build of Batfish, or {@link #UNKNOWN_VERSION} if the version
   * could not be detected.
   */
  public static String getVersion() {
    return getPropertiesVersion(PROPERTIES_PATH, "batfish_version");
  }

  /** Returns the version corresponding to the specified key, in the specified properties file */
  public static String getPropertiesVersion(String propertiesPath, String key) {
    try {
      Configuration config = new Configurations().properties(propertiesPath);
      String version = config.getString(key);
      if (version.contains("project.version")) {
        // For whatever reason, resource filtering didn't work.
        return UNKNOWN_VERSION;
      }
      return version;
    } catch (Exception e) {
      return UNKNOWN_VERSION;
    }
  }

  // Suppress instantiation of utility class.
  private Version() {}
}
