package org.batfish.question.searchfilters;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.Configuration;

/** A mock Batfish for search filters tests. */
final class MockBatfish extends IBatfishTestAdapter {
  private final Configuration _baseConfig;
  private final Configuration _deltaConfig;

  MockBatfish(Configuration baseConfig) {
    _baseConfig = baseConfig;
    _deltaConfig = null;
  }

  MockBatfish(Configuration baseConfig, Configuration deltaConfig) {
    _baseConfig = baseConfig;
    _deltaConfig = deltaConfig;
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
    assertTrue(snapshot.equals(getSnapshot()) || snapshot.equals(getReferenceSnapshot()));
    Configuration config = snapshot.equals(getSnapshot()) ? _baseConfig : _deltaConfig;
    return ImmutableSortedMap.of(config.getHostname(), config);
  }

  @Override
  public String getFlowTag() {
    return "flowTag";
  }
}
