package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents PORT object: https://github.com/Azure/SONiC/wiki/Configuration#port */
@ParametersAreNonnullByDefault
public class Port implements Serializable {
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_MTU = "mtu";
  private static final String PROP_ADMIN_STATUS = "admin_status";

  private @Nullable final String _description;
  private @Nullable final Integer _mtu;
  private @Nullable final Boolean _adminStatus;

  public @Nonnull Optional<Boolean> getAdminStatus() {
    return Optional.ofNullable(_adminStatus);
  }

  public @Nonnull Optional<String> getDescription() {
    return Optional.ofNullable(_description);
  }

  public @Nonnull Optional<Integer> getMtu() {
    return Optional.ofNullable(_mtu);
  }

  @JsonCreator
  private static Port create(
      @Nullable @JsonProperty(PROP_ADMIN_STATUS) String adminStatus,
      @Nullable @JsonProperty(PROP_DESCRIPTION) String description,
      @Nullable @JsonProperty(PROP_MTU) String mtu) {
    return Port.builder()
        // up => true, everything else is false
        .setAdminStatus(Optional.ofNullable(adminStatus).map("up"::equals).orElse(null))
        .setDescription(description)
        .setMtu(Optional.ofNullable(mtu).map(Integer::parseInt).orElse(null))
        .build();
  }

  private Port(@Nullable Boolean adminStatus, @Nullable String description, @Nullable Integer mtu) {
    _adminStatus = adminStatus;
    _description = description;
    _mtu = mtu;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Port)) {
      return false;
    }
    Port that = (Port) o;
    return Objects.equals(_adminStatus, that._adminStatus)
        && Objects.equals(_description, that._description)
        && Objects.equals(_mtu, that._mtu);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_adminStatus, _description, _mtu);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("adminStatus", _adminStatus)
        .add("description", _description)
        .add("mtu", _mtu)
        .toString();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String _description;
    private Integer _mtu;
    private Boolean _adminStatus;

    public Builder setAdminStatus(@Nullable Boolean adminStatus) {
      this._adminStatus = adminStatus;
      return this;
    }

    public Builder setDescription(@Nullable String description) {
      this._description = description;
      return this;
    }

    public Builder setMtu(@Nullable Integer mtu) {
      this._mtu = mtu;
      return this;
    }

    public Port build() {
      return new Port(_adminStatus, _description, _mtu);
    }
  }
}
