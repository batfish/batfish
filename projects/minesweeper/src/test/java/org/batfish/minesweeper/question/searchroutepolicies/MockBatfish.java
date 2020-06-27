package org.batfish.minesweeper.question.searchroutepolicies;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Topology;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;

final class MockBatfish extends IBatfishTestAdapter {
  private final SortedMap<String, Configuration> _baseConfigs;
  private final SortedMap<String, Configuration> _deltaConfigs;

  MockBatfish(
      SortedMap<String, Configuration> baseConfigs, SortedMap<String, Configuration> deltaConfigs) {
    _baseConfigs = ImmutableSortedMap.copyOf(baseConfigs);
    _deltaConfigs = ImmutableSortedMap.copyOf(deltaConfigs);
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
    if (getSnapshot().equals(snapshot)) {
      return _baseConfigs;
    } else if (getReferenceSnapshot().equals(snapshot)) {
      return _deltaConfigs;
    }
    throw new IllegalArgumentException("Unknown snapshot: " + snapshot);
  }

  @Override
  public TopologyProvider getTopologyProvider() {
    return new TopologyProviderTestAdapter(this) {
      @Override
      public Topology getInitialLayer3Topology(NetworkSnapshot networkSnapshot) {
        return Topology.EMPTY;
      }
    };
  }

  @Override
  public Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot networkSnapshot) {
    return ImmutableMap.of();
  }
}
