package org.batfish.coordinator.authorizer;

/** An {@link Authorizer} that approves everything. Useful for testing and private deployments. */
public final class NoneAuthorizer implements Authorizer {
  public static final NoneAuthorizer INSTANCE = new NoneAuthorizer();

  @Override
  public void authorizeContainer(String apiKey, String containerName) {}

  @Override
  public boolean isAccessibleNetwork(String apiKey, String containerName, boolean logError) {
    return true;
  }

  @Override
  public boolean isValidWorkApiKey(String apiKey) {
    return true;
  }

  // Prevent construction
  private NoneAuthorizer() {}

  @Override
  public String toString() {
    return NoneAuthorizer.class.getSimpleName();
  }
}
