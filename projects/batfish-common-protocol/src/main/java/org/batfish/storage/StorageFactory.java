package org.batfish.storage;

import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;

public class StorageFactory {

  /**
   * Gets a new instance of Storage implementation using specified settings
   *
   * @param settings Settings for initializing storage. Settings contains storage implementaion type
   *     in property {@link BfConsts#PROP_STORAGE_IMPLEMENTATION} which can be one of {@link
   *     StorageType}
   * @return Corresponding storage implementation
   */
  public static Storage getImplementation(Map<String, Object> settings) {
    switch ((StorageType) settings.get(BfConsts.PROP_STORAGE_IMPLEMENTATION)) {
      case FILE:
        return FileStorageImpl.create(settings);
      default:
        throw new BatfishException(
            String.format(
                "No valid implementation found for %s",
                settings.get(BfConsts.PROP_STORAGE_IMPLEMENTATION)));
    }
  }

  // Enum for the different implementations of storage
  public enum StorageType {
    FILE,
  }
}
