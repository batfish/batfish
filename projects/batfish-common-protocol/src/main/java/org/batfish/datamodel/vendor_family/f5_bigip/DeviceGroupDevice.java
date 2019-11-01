package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for a {@link Device} within a {@link DeviceGroup}. */
public final class DeviceGroupDevice implements Serializable {

  public static final class Builder {

    public @Nonnull DeviceGroupDevice build() {
      checkArgument(_name != null, "Missing %s", "name");
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

  public @Nonnull String getName() {
    return _name;
  }

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

  private final @Nonnull String _name;
  private boolean _setSyncLeader;

  private DeviceGroupDevice(String name, boolean setSyncLeader) {
    _name = name;
    _setSyncLeader = setSyncLeader;
  }
}
