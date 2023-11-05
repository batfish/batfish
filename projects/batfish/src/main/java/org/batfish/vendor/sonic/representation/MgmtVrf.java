package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the settings of a management VRF object:
 * https://github.com/Azure/SONiC/wiki/Configuration#management-vrf
 */
public class MgmtVrf implements Serializable {
  private static final String PROP_MGMT_VRF_ENABLED = "mgmtVrfEnabled";
  private static final String PROP_IN_BAND_MGMT_ENABLED = "in_band_mgmt_enabled";

  private final @Nullable Boolean _mgmtVrfEnabled;

  public @Nonnull Optional<Boolean> getMgmtVrfEnabled() {
    return Optional.ofNullable(_mgmtVrfEnabled);
  }

  @SuppressWarnings("unused") // "parse" and ignore PROP_IN_BAND_MGMT_ENABLED
  @JsonCreator
  private static @Nonnull MgmtVrf create(
      @JsonProperty(PROP_IN_BAND_MGMT_ENABLED) @Nullable String inBandMgmtEnabled,
      @JsonProperty(PROP_MGMT_VRF_ENABLED) @Nullable String mgmtVrfEnabled) {
    return MgmtVrf.builder()
        .setMgmtVrfEnabled(Optional.ofNullable(mgmtVrfEnabled).map("true"::equals).orElse(null))
        .build();
  }

  private MgmtVrf(@Nullable Boolean mgmtVrfEnabled) {
    _mgmtVrfEnabled = mgmtVrfEnabled;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MgmtVrf)) {
      return false;
    }
    MgmtVrf that = (MgmtVrf) o;
    return Objects.equals(_mgmtVrfEnabled, that._mgmtVrfEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_mgmtVrfEnabled);
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("mgmtVrfEnabled", _mgmtVrfEnabled)
        .toString();
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Boolean _mgmtVrfEnabled;

    public @Nonnull Builder setMgmtVrfEnabled(@Nullable Boolean mgmtVrfEnabled) {
      this._mgmtVrfEnabled = mgmtVrfEnabled;
      return this;
    }

    public @Nonnull MgmtVrf build() {
      return new MgmtVrf(_mgmtVrfEnabled);
    }
  }
}
