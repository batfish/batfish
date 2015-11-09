package org.batfish.coordinator.authorizer;

//a pass through authorizer that answers yes to everything
//useful for testing and private deployments
public class NoneAuthorizer implements Authorizer {

   @Override
   public boolean isAccessibleContainer(String apiKey, String containerName) {
      return true;
   }

   @Override
   public boolean isValidWorkApiKey(String apiKey) {
      return true;
   }

   @Override
   public void authorizeContainer(String apiKey, String containerName) {
      return;
   }
}
