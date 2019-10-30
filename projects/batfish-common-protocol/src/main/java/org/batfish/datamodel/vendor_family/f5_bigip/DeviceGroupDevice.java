package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for a {@link Device} within a {@link DeviceGroup}. */
public final class DeviceGroupDevice implements Serializable {

  public static final class Builder {

    public @Nonnull DeviceGroupDevice build() {
      checkArgument(_name != null, "Missing %s", PROP_NAME);
      return new DeviceGroupDevice(_name, _setSyncLeader);
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setSetSyncLeader(boolean setSyncLeader) {
      _setSyncLeader = setSyncLeader;
      return this;
    }

    private @Nullable String _name;
    private boolean _setSyncLeader;

    private Builder() {}
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_SET_SYNC_LEADER)
  public boolean getSetSyncLeader() {
    return _setSyncLeader;
  }

  public void setSetSyncLeader(boolean setSyncLeader) {
    _setSyncLeader = setSyncLeader;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DeviceGroupDevice)) {
      return false;
    }
    DeviceGroupDevice rhs = (DeviceGroupDevice) obj;
    return _name.equals(rhs._name) && _setSyncLeader == rhs._setSyncLeader;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _setSyncLeader);
  }

  private static final String PROP_NAME = "name";
  private static final String PROP_SET_SYNC_LEADER = "setSyncLeader";

  @JsonCreator
  private static @Nonnull DeviceGroupDevice create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_SET_SYNC_LEADER) @Nullable Boolean setSyncLeader) {
    Builder builder = builder();
    ofNullable(name).ifPresent(builder::setName);
    ofNullable(setSyncLeader).ifPresent(builder::setSetSyncLeader);
    return builder.build();
  }

  private final @Nonnull String _name;
  private boolean _setSyncLeader;

  private DeviceGroupDevice(String name, boolean setSyncLeader) {
    _name = name;
    _setSyncLeader = setSyncLeader;
  }
}
