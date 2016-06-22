package org.batfish.coordinator.authorizer;

public interface Authorizer {

   public enum Type {
      database,
      file,
      none
   }

   void authorizeContainer(String apiKey, String containerName)
         throws Exception;

   boolean isAccessibleContainer(String apiKey, String containerName,
         boolean logError) throws Exception;

   boolean isValidWorkApiKey(String apiKey) throws Exception;
}
