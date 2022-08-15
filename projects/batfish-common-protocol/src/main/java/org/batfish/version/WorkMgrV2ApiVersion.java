package org.batfish.version;

import static org.batfish.common.Version.getPropertiesVersion;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;

@AutoService(Versioned.class)
public class WorkMgrV2ApiVersion implements Versioned {
  @VisibleForTesting static final String PROPERTIES_PATH = "org/batfish/common/common.properties";
  private static final String VERSION_KEY = "workmgrv2_api_version";

  private static final String NAME = "workmgrv2_api_version";
  private static final String VERSION = getPropertiesVersion(PROPERTIES_PATH, VERSION_KEY);

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getVersion() {
    return VERSION;
  }

  public static @Nonnull String getVersionStatic() {
    return VERSION;
  }
}
