package org.batfish.representation.palo_alto.application_definitions;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Helper class to create {@link ApplicationDefinition}s and dependent objects for testing. */
public class TestApplicationDefinitions {
  @VisibleForTesting
  public static ApplicationDefinition createApplicationDefinition(
      @Nonnull String name,
      @Nullable String applicationContainer,
      @Nullable String parentApp,
      @Nullable UseApplications useApplications,
      @Nullable UseApplications implicitUseApplications,
      @Nullable Default defaultVal) {
    return new ApplicationDefinition(
        name,
        applicationContainer,
        parentApp,
        useApplications,
        implicitUseApplications,
        defaultVal);
  }

  @VisibleForTesting
  public static Default createDefault(
      @Nullable Port port, @Nullable String identByIpProtocol, @Nullable String identByIcmpType) {
    return new Default(port, identByIpProtocol, identByIcmpType);
  }

  @VisibleForTesting
  public static Port createPort(@Nonnull List<String> member) {
    return new Port(member);
  }

  // Prevent instantiation
  private TestApplicationDefinitions() {}
}
