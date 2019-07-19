package org.batfish.version;

import com.google.auto.service.AutoService;
import org.batfish.common.plugin.Plugin;

@AutoService(Plugin.class)
public class BatfishVersionPlugin extends VersionPlugin {
  @Override
  protected String getName() {
    return "Batfish";
  }

  @Override
  protected String getVersion() {
    return "0.36.0";
  }
}
