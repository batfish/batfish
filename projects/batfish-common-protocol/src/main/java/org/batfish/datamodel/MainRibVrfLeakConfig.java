package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** VRF leaking config that leaks routes between main RIBs */
@ParametersAreNonnullByDefault
public final class MainRibVrfLeakConfig implements Serializable {

  /**
   * Name of the import policy to apply to imported routes when leaking. If {@code null} no policy
   * is applied, all routes are allowed.
   */
  @Nullable
  @JsonProperty(PROP_IMPORT_POLICY)
  public String getImportPolicy() {
    return _importPolicy;
  }

  /** Name of the source VRF from which to copy routes. */
  @Nonnull
  @JsonProperty(PROP_IMPORT_FROM_VRF)
  public String getImportFromVrf() {
    return _importFromVrf;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MainRibVrfLeakConfig)) {
      return false;
    }
    MainRibVrfLeakConfig that = (MainRibVrfLeakConfig) o;
    return _importFromVrf.equals(that._importFromVrf)
        && Objects.equals(_importPolicy, that._importPolicy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_importPolicy, _importFromVrf);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(MainRibVrfLeakConfig.class)
        .omitNullValues()
        .add("importPolicy", _importPolicy)
        .add("importFromVrf", _importFromVrf)
        .toString();
  }

  private static final String PROP_IMPORT_FROM_VRF = "importFromVrf";
  private static final String PROP_IMPORT_POLICY = "importPolicy";

  @Nullable private final String _importPolicy;
  @Nonnull private final String _importFromVrf;

  private MainRibVrfLeakConfig(@Nullable String importPolicy, String importFromVrf) {
    _importPolicy = importPolicy;
    _importFromVrf = importFromVrf;
  }

  @JsonCreator
  private static MainRibVrfLeakConfig create(
      @Nullable @JsonProperty(PROP_IMPORT_FROM_VRF) String importFromVrf,
      @Nullable @JsonProperty(PROP_IMPORT_POLICY) String importPolicy) {
    checkArgument(importFromVrf != null, String.format("Missing %s", PROP_IMPORT_FROM_VRF));
    return new MainRibVrfLeakConfig(importPolicy, importFromVrf);
  }

  @ParametersAreNonnullByDefault
  public static class Builder {

    public Builder setImportPolicy(@Nullable String importPolicy) {
      _importPolicy = importPolicy;
      return this;
    }

    public Builder setImportFromVrf(@Nullable String importFromVrf) {
      _importFromVrf = importFromVrf;
      return this;
    }

    public MainRibVrfLeakConfig build() {
      checkArgument(_importFromVrf != null, "Missing %s", PROP_IMPORT_FROM_VRF);
      return new MainRibVrfLeakConfig(_importPolicy, _importFromVrf);
    }

    @Nullable private String _importPolicy;
    @Nullable private String _importFromVrf;

    private Builder() {}
  }
}
