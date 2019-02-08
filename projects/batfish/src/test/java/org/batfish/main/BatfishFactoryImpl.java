package org.batfish.main;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.junit.rules.TemporaryFolder;

/** Implementation of {@link BatfishFactory}. */
@AutoService(BatfishFactory.class)
public final class BatfishFactoryImpl implements BatfishFactory {
  @Override
  public IBatfish getBatfish(
      SortedMap<String, Configuration> configurations, @Nonnull TemporaryFolder tempFolder)
      throws IOException {
    return BatfishTestUtils.getBatfish(configurations, tempFolder);
  }

  @Override
  public IBatfish getBatfish(
      @Nonnull SortedMap<String, Configuration> baseConfigs,
      @Nonnull SortedMap<String, Configuration> deltaConfigs,
      @Nonnull TemporaryFolder tempFolder)
      throws IOException {
    return BatfishTestUtils.getBatfish(baseConfigs, deltaConfigs, tempFolder);
  }
}
