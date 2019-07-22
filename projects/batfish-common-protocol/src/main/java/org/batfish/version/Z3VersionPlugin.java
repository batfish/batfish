package org.batfish.version;

import static org.batfish.common.Version.getZ3Version;

import com.google.auto.service.AutoService;
import org.batfish.common.plugin.Plugin;

@AutoService(Plugin.class)
public class Z3VersionPlugin extends VersionPlugin {
  @Override
  protected String getName() {
    return "Z3";
  }

  @Override
  protected String getVersion() {
    return getZ3Version();
  }
}
