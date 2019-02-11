package org.batfish.question.searchfilters;

import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import java.util.Stack;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.Configuration;

/** A mock Batfish for search filters tests. */
final class MockBatfish extends IBatfishTestAdapter {
  private final Configuration _baseConfig;
  private final Configuration _deltaConfig;

  // true if base, false if delta.
  private final Stack<Boolean> _snapshotStack = new Stack<>();

  MockBatfish(Configuration baseConfig) {
    _baseConfig = baseConfig;
    _deltaConfig = null;
    _snapshotStack.push(true);
  }

  MockBatfish(Configuration baseConfig, Configuration deltaConfig) {
    _baseConfig = baseConfig;
    _deltaConfig = deltaConfig;
    _snapshotStack.push(true);
  }

  @Override
  public void pushBaseSnapshot() {
    _snapshotStack.push(true);
  }

  @Override
  public void pushDeltaSnapshot() {
    _snapshotStack.push(false);
  }

  @Override
  public void popSnapshot() {
    _snapshotStack.pop();
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations() {
    Configuration config = _snapshotStack.peek() ? _baseConfig : _deltaConfig;
    return ImmutableSortedMap.of(config.getHostname(), config);
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
    return loadConfigurations();
  }

  @Override
  public String getFlowTag() {
    return "flowTag";
  }
}
