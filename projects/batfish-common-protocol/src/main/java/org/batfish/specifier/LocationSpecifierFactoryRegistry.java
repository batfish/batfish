package org.batfish.specifier;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.batfish.common.BatfishException;

public class LocationSpecifierFactoryRegistry {
  private Map<String, LocationSpecifierFactory> _locationResolvers = new HashMap<>();

  public LocationSpecifierFactoryRegistry() {
    ServiceLoader.load(LocationSpecifierFactory.class)
        .iterator()
        .forEachRemaining(
            locationSpecifierFactory ->
                _locationResolvers.put(
                    locationSpecifierFactory.getName(), locationSpecifierFactory));
  }

  public LocationSpecifierFactory getLocationSpecifierFactory(String name) {
    if (!_locationResolvers.containsKey(name)) {
      throw new BatfishException("No LocationSpecifierFactory found with name = " + name);
    }
    return _locationResolvers.get(name);
  }
}
