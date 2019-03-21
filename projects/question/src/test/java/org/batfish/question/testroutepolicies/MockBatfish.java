package org.batfish.question.testroutepolicies;

import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.Configuration;

final class MockBatfish extends IBatfishTestAdapter {
  private final SortedMap<String, Configuration> _configs;

  MockBatfish(SortedMap<String, Configuration> configs) {
    _configs = ImmutableSortedMap.copyOf(configs);
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations() {
    return _configs;
  }
}
