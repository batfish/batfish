package org.batfish.representation.palo_alto.application_definitions;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Data model for a Palo Alto application definition. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ApplicationDefinition {
  private static final String PROP_NAME = "@name";
  private static final String PROP_APPLICATION_CONTAINER = "application-container";
  private static final String PROP_PARENT_APP = "parent-app";
  private static final String PROP_USE_APPLICATIONS = "use-applications";
  private static final String PROP_IMPLICIT_USE_APPLICATIONS = "implicit-use-applications";
  private static final String PROP_DEFAULT = "default";

  /** Get the name of the application. */
  @Nonnull
  public String getName() {
    return _name;
  }

  /**
   * Get the application container for this application. Appears similar in function to an {@link
   * org.batfish.representation.palo_alto.ApplicationGroup}.
   */
  @Nullable
  public String getApplicationContainer() {
    return _applicationContainer;
  }

  @Nullable
  public String getParentApp() {
    return _parentApp;
  }

  @Nullable
  public UseApplications getUseApplications() {
    return _useApplications;
  }

  @Nullable
  public UseApplications getImplicitUseApplications() {
    return _implicitUseApplications;
  }

  /** Get information about default application properties, specifically IP protocol and port. */
  @Nullable
  public Default getDefault() {
    return _default;
  }

  @JsonCreator
  private static @Nonnull ApplicationDefinition create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_APPLICATION_CONTAINER) @Nullable String applicationContainer,
      @JsonProperty(PROP_PARENT_APP) @Nullable String parentApp,
      @JsonProperty(PROP_USE_APPLICATIONS) @Nullable UseApplications useApplications,
      @JsonProperty(PROP_IMPLICIT_USE_APPLICATIONS) @Nullable
          UseApplications implicitUseApplications,
      @JsonProperty(PROP_DEFAULT) @Nullable Default defaultVal) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    return new ApplicationDefinition(
        name,
        applicationContainer,
        parentApp,
        useApplications,
        implicitUseApplications,
        defaultVal);
  }

  @VisibleForTesting
  ApplicationDefinition(
      String name,
      @Nullable String applicationContainer,
      @Nullable String parentApp,
      @Nullable UseApplications useApplications,
      @Nullable UseApplications implicitUseApplications,
      @Nullable Default defaultVal) {
    _name = name;
    _applicationContainer = applicationContainer;
    _parentApp = parentApp;
    _useApplications = useApplications;
    _implicitUseApplications = implicitUseApplications;
    _default = defaultVal;
  }

  @Nonnull private final String _name;
  @Nullable private final String _applicationContainer;
  @Nullable private final String _parentApp;
  @Nullable private final UseApplications _useApplications;
  @Nullable private final UseApplications _implicitUseApplications;
  @Nullable private final Default _default;
}
