package org.batfish.coordinator.version;

import static org.batfish.common.Version.getPropertiesVersion;

import com.google.auto.service.AutoService;
import javax.annotation.Nonnull;
import org.batfish.version.Versioned;

@AutoService(Versioned.class)
public final class ApiVersion implements Versioned {
  private static final String PROPERTIES_PATH =
      "org/batfish/coordinator/version/version.properties";

  private static final String VERSION_KEY = "api_version";

  private static final String NAME = "api_version";
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
