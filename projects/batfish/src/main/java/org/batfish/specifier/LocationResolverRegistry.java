package org.batfish.specifier;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.batfish.common.BatfishException;

public class LocationResolverRegistry {
  private static Map<String, LocationSpecifierFactory> _locationResolvers = new HashMap<>();

  static {
    ServiceLoader.load(LocationSpecifierFactory.class)
        .iterator()
        .forEachRemaining(
            locationSpecifierFactory ->
                _locationResolvers.put(
                    locationSpecifierFactory.getName(), locationSpecifierFactory));
  }

  public static LocationSpecifierFactory getLocationResolver(String name) {
    if (!_locationResolvers.containsKey(name)) {
      throw new BatfishException("No LocationSpecifierFactory found with name = " + name);
    }
    return _locationResolvers.get(name);
  }
}
