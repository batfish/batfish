package org.batfish.main;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.junit.rules.TemporaryFolder;

public interface BatfishFactory {
  static BatfishFactory load() {
    List<BatfishFactory> impls = ImmutableList.copyOf(ServiceLoader.load(BatfishFactory.class));
    checkState(!impls.isEmpty(), "No BatfishFactory implementation found");
    checkState(
        impls.size() == 1, String.format("Only 1 BatfishFactory allowed. Found %d", impls.size()));
    return impls.get(0);
  }

  /**
   * Get a new Batfish instance with given configurations, tempFolder should be present for
   * non-empty configurations
   *
   * @param configurations Map of all Configuration Name -&gt; Configuration Object
   * @param tempFolder Temporary folder to be used to files required for Batfish
   * @return New Batfish instance
   */
  IBatfish getBatfish(
      SortedMap<String, Configuration> configurations, @Nonnull TemporaryFolder tempFolder)
      throws IOException;

  /**
   * Get a new Batfish instance with given base and delta configurations, tempFolder should be
   * present for non-empty configurations
   *
   * @param baseConfigs Map of all Configuration Name -&gt; Configuration Object
   * @param deltaConfigs Map of all Configuration Name -&gt; Configuration Object
   * @param tempFolder Temporary folder to be used to files required for Batfish
   * @return New Batfish instance
   */
  IBatfish getBatfish(
      @Nonnull SortedMap<String, Configuration> baseConfigs,
      @Nonnull SortedMap<String, Configuration> deltaConfigs,
      @Nonnull TemporaryFolder tempFolder)
      throws IOException;
}
