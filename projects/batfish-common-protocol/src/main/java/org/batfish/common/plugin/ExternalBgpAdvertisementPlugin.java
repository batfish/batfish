package org.batfish.common.plugin;

import org.batfish.datamodel.collections.AdvertisementSet;

public abstract class ExternalBgpAdvertisementPlugin extends BatfishPlugin
    implements IExternalBgpAdvertisementPlugin {

  @Override
  protected final void batfishPluginInitialize() {
    _batfish.registerExternalBgpAdvertisementPlugin(this);
    externalBgpAdvertisementPluginInitialize();
  }

  protected abstract void externalBgpAdvertisementPluginInitialize();

  public abstract AdvertisementSet loadExternalBgpAdvertisements();
}
