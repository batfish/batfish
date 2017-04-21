package org.batfish.common.plugin;

import org.batfish.datamodel.collections.AdvertisementSet;

public abstract class ExternalBgpAdvertisementPlugin extends BatfishPlugin
      implements IExternalBgpAdvertisementPlugin,
      Comparable<ExternalBgpAdvertisementPlugin> {

   @Override
   protected final void batfishPluginInitialize() {
      _batfish.registerExternalBgpAdvertisementPlugin(this);
      externalBgpAdvertisementPluginInitialize();
   }

   @Override
   public int compareTo(ExternalBgpAdvertisementPlugin o) {
      return getClass().getCanonicalName()
            .compareTo(o.getClass().getCanonicalName());
   }

   @Override
   public boolean equals(Object obj) {
      return getClass().equals(obj.getClass());
   }

   protected abstract void externalBgpAdvertisementPluginInitialize();

   @Override
   public int hashCode() {
      return getClass().hashCode();
   }

   public abstract AdvertisementSet loadExternalBgpAdvertisements();

}
