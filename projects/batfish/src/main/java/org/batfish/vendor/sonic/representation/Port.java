package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents PORT object: https://github.com/Azure/SONiC/wiki/Configuration#port */
public class Port implements Serializable {
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_MTU = "mtu";
  private static final String PROP_ADMIN_STATUS = "admin_status";

  private @Nullable final String _description;
  private @Nullable final Integer _mtu;
  private @Nullable final Boolean _adminStatusUp;

  public @Nonnull Optional<Boolean> getAdminStatusUp() {
    return Optional.ofNullable(_adminStatusUp);
  }

  public @Nonnull Optional<String> getDescription() {
    return Optional.ofNullable(_description);
  }

  public @Nonnull Optional<Integer> getMtu() {
    return Optional.ofNullable(_mtu);
  }

  @JsonCreator
  private @Nonnull static Port create(
      @Nullable @JsonProperty(PROP_ADMIN_STATUS) String adminStatus,
      @Nullable @JsonProperty(PROP_DESCRIPTION) String description,
      @Nullable @JsonProperty(PROP_MTU) String mtu) {
    return Port.builder()
        .setAdminStatusUp(Optional.ofNullable(adminStatus).map("up"::equals).orElse(null))
        .setDescription(description)
        .setMtu(Optional.ofNullable(mtu).map(Integer::parseInt).orElse(null))
        .build();
  }

  private @Nonnull Port(
      @Nullable Boolean adminStatus, @Nullable String description, @Nullable Integer mtu) {
    _adminStatusUp = adminStatus;
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
    return Objects.equals(_adminStatusUp, that._adminStatusUp)
        && Objects.equals(_description, that._description)
        && Objects.equals(_mtu, that._mtu);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_adminStatusUp, _description, _mtu);
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("adminStatusUp", _adminStatusUp)
        .add("description", _description)
        .add("mtu", _mtu)
        .toString();
  }

  public @Nonnull static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String _description;
    private Integer _mtu;
    private Boolean _adminStatusUp;

    public @Nonnull Builder setAdminStatusUp(@Nullable Boolean adminStatusUp) {
      this._adminStatusUp = adminStatusUp;
      return this;
    }

    public @Nonnull Builder setDescription(@Nullable String description) {
      this._description = description;
      return this;
    }

    public @Nonnull Builder setMtu(@Nullable Integer mtu) {
      this._mtu = mtu;
      return this;
    }

    public @Nonnull Port build() {
      return new Port(_adminStatusUp, _description, _mtu);
    }
  }
}
