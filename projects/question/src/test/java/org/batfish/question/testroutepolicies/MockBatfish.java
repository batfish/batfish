package org.batfish.question.testroutepolicies;

import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import java.util.Stack;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.Configuration;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;

final class MockBatfish extends IBatfishTestAdapter {
  private final SortedMap<String, Configuration> _baseConfigs;
  private final SortedMap<String, Configuration> _deltaConfigs;

  private static final NetworkSnapshot BASE_SNAPSHOT =
      new NetworkSnapshot(new NetworkId("network"), new SnapshotId("base"));
  private static final NetworkSnapshot DELTA_SNAPSHOT =
      new NetworkSnapshot(new NetworkId("network"), new SnapshotId("delta"));

  private Stack<NetworkSnapshot> _snapshots = new Stack<>();

  MockBatfish(
      SortedMap<String, Configuration> baseConfigs, SortedMap<String, Configuration> deltaConfigs) {
    _baseConfigs = ImmutableSortedMap.copyOf(baseConfigs);
    _deltaConfigs = ImmutableSortedMap.copyOf(deltaConfigs);
    _snapshots.push(BASE_SNAPSHOT);
  }

  @Override
  public void pushBaseSnapshot() {
    _snapshots.push(BASE_SNAPSHOT);
  }

  @Override
  public void pushDeltaSnapshot() {
    _snapshots.push(DELTA_SNAPSHOT);
  }

  @Override
  public void popSnapshot() {
    _snapshots.pop();
  }

  @Override
  public NetworkSnapshot getNetworkSnapshot() {
    return _snapshots.peek();
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations() {
    return loadConfigurations(getNetworkSnapshot());
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
    if (BASE_SNAPSHOT.equals(snapshot)) {
      return _baseConfigs;
    } else if (DELTA_SNAPSHOT.equals(snapshot)) {
      return _deltaConfigs;
    }
    throw new IllegalArgumentException("Unknown snapshot: " + snapshot);
  }
}
