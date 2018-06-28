package org.batfish.coordinator.authorizer;

public interface Authorizer {

  enum Type {
    database,
    file,
    none
  }

  void authorizeContainer(String apiKey, String containerName);

  boolean isAccessibleContainer(String apiKey, String containerName, boolean logError);

  boolean isValidWorkApiKey(String apiKey);
}
