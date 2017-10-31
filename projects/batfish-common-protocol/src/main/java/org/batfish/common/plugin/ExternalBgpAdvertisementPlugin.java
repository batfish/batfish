package org.batfish.common.plugin;

import java.util.LinkedHashSet;
import org.batfish.datamodel.BgpAdvertisement;

public abstract class ExternalBgpAdvertisementPlugin extends BatfishPlugin
    implements IExternalBgpAdvertisementPlugin {

  @Override
  protected final void batfishPluginInitialize() {
    _batfish.registerExternalBgpAdvertisementPlugin(this);
    externalBgpAdvertisementPluginInitialize();
  }

  protected abstract void externalBgpAdvertisementPluginInitialize();

  public abstract LinkedHashSet<BgpAdvertisement> loadExternalBgpAdvertisements();
}
