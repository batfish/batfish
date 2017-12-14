package org.batfish.coordinator.authorizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** An {@link Authorizer} backed by an in-memory map. Useful for testing. */
public class MapAuthorizer implements Authorizer {
  private HashMap<String, List<String>> _permissionMap;

  public MapAuthorizer() {
    _permissionMap = new HashMap<>();
  }

  @Override
  public void authorizeContainer(String apiKey, String containerName) {
    _permissionMap.computeIfAbsent(
        apiKey,
        val -> {
          List<String> list = new ArrayList<>();
          list.add(containerName);
          return list;
        });
  }

  @Override
  public boolean isAccessibleContainer(String apiKey, String containerName, boolean logError) {
    List<String> list = _permissionMap.get(apiKey);
    return list != null && list.contains(containerName);
  }

  @Override
  public boolean isValidWorkApiKey(String apiKey) {
    return _permissionMap.containsKey(apiKey);
  }
}
