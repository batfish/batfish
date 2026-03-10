package org.batfish.minesweeper;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;

public final class ConfigAtomicPredicatesTestUtils {

  /** Create a {@link ConfigAtomicPredicates} for all routing policies on the named device. */
  public static ConfigAtomicPredicates forDevice(
      IBatfish batfish, NetworkSnapshot snapshot, String hostname) {
    return forDevice(batfish, snapshot, hostname, null, null);
  }

  /**
   * Create a {@link ConfigAtomicPredicates} for all routing policies on the named device.
   *
   * @param batfish the batfish object
   * @param snapshot the current snapshot
   * @param hostname the name of the device whose configuration is being analyzed
   * @param extraCommunities additional community regexes to track, from user-defined constraints
   * @param extraAsPathRegexes additional as-path regexes to track, from user-defined constraints
   */
  public static ConfigAtomicPredicates forDevice(
      IBatfish batfish,
      NetworkSnapshot snapshot,
      String hostname,
      @Nullable Set<CommunityVar> extraCommunities,
      @Nullable Set<String> extraAsPathRegexes) {
    Map<String, Configuration> configs = batfish.loadConfigurations(snapshot);
    @Nullable Configuration config = configs.get(hostname);
    checkArgument(config != null, "Missing configuration for device %s", hostname);
    return new ConfigAtomicPredicates(
        ImmutableList.of(new SimpleImmutableEntry<>(config, config.getRoutingPolicies().values())),
        firstNonNull(extraCommunities, ImmutableSet.of()),
        firstNonNull(extraAsPathRegexes, ImmutableSet.of()));
  }

  private ConfigAtomicPredicatesTestUtils() {}
}
