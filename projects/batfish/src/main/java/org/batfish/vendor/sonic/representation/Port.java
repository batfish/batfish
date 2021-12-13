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
  private static final String PROP_ADMIN_STATUS = "admin_status";
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_MTU = "mtu";
  private static final String PROP_SPEED = "speed";

  private @Nullable final Boolean _adminStatusUp;
  private @Nullable final String _description;
  private @Nullable final Integer _mtu;
  private @Nullable final Integer _speed;

  public @Nonnull Optional<Boolean> getAdminStatusUp() {
    return Optional.ofNullable(_adminStatusUp);
  }

  public @Nonnull Optional<String> getDescription() {
    return Optional.ofNullable(_description);
  }

  public @Nonnull Optional<Integer> getMtu() {
    return Optional.ofNullable(_mtu);
  }

  public @Nonnull Optional<Integer> getSpeed() {
    return Optional.ofNullable(_speed);
  }

  @JsonCreator
  private @Nonnull static Port create(
      @Nullable @JsonProperty(PROP_ADMIN_STATUS) String adminStatus,
      @Nullable @JsonProperty(PROP_DESCRIPTION) String description,
      @Nullable @JsonProperty(PROP_MTU) String mtu,
      @Nullable @JsonProperty(PROP_SPEED) String speed) {
    return Port.builder()
        .setAdminStatusUp(Optional.ofNullable(adminStatus).map("up"::equals).orElse(null))
        .setDescription(description)
        .setMtu(Optional.ofNullable(mtu).map(Integer::parseInt).orElse(null))
        .setSpeed(Optional.ofNullable(speed).map(Integer::parseInt).orElse(null))
        .build();
  }

  private @Nonnull Port(
      @Nullable Boolean adminStatus,
      @Nullable String description,
      @Nullable Integer mtu,
      @Nullable Integer speed) {
    _adminStatusUp = adminStatus;
    _description = description;
    _mtu = mtu;
    _speed = speed;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Port)) {
      return false;
    }
    Port that = (Port) o;
    return Objects.equals(_adminStatusUp, that._adminStatusUp)
        && Objects.equals(_description, that._description)
        && Objects.equals(_mtu, that._mtu)
        && Objects.equals(_speed, that._speed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_adminStatusUp, _description, _mtu, _speed);
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("adminStatusUp", _adminStatusUp)
        .add("description", _description)
        .add("mtu", _mtu)
        .add("speed", _speed)
        .toString();
  }

  public @Nonnull static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Boolean _adminStatusUp;
    private String _description;
    private Integer _mtu;
    private Integer _speed;

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

    public @Nonnull Builder setSpeed(@Nullable Integer speed) {
      this._speed = speed;
      return this;
    }

    public @Nonnull Port build() {
      return new Port(_adminStatusUp, _description, _mtu, _speed);
    }
  }
}
