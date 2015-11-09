package org.batfish.coordinator.authorizer;

public interface Authorizer {

   public enum Type {
      none,
      file,
      database
   }
   
   boolean isAccessibleContainer(String apiKey, String containerName);
   boolean isValidWorkApiKey(String apiKey);
   void authorizeContainer(String apiKey, String containerName);
}
