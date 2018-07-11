package org.batfish.question.routes;

import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Comparator.naturalOrder;

import java.util.Map.Entry;
import java.util.SortedMap;
import org.batfish.common.BatfishLogger;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.NetworkConfigurations;

public class MockBatfish extends IBatfishTestAdapter {

  private final NetworkConfigurations _configs;
  private final DataPlane _dp;

  public MockBatfish(NetworkConfigurations configs, DataPlane dp) {
    _configs = configs;
    _dp = dp;
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations() {
    return _configs
        .getMap()
        .entrySet()
        .stream()
        .collect(toImmutableSortedMap(naturalOrder(), Entry::getKey, Entry::getValue));
  }

  @Override
  public DataPlane loadDataPlane() {
    return _dp;
  }

  @Override
  public BatfishLogger getLogger() {
    return null;
  }
}
