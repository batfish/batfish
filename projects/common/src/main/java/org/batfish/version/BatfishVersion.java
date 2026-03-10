package org.batfish.version;

import static org.batfish.common.Version.getPropertiesVersion;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;

@AutoService(Versioned.class)
public class BatfishVersion implements Versioned {
  @VisibleForTesting static final String PROPERTIES_PATH = "org/batfish/common/common.properties";
  private static final String VERSION_KEY = "batfish_version";

  private static final String NAME = "Batfish";
  private static final String VERSION = getPropertiesVersion(PROPERTIES_PATH, VERSION_KEY);

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getVersion() {
    return VERSION;
  }

  public static String getVersionStatic() {
    return VERSION;
  }
}
