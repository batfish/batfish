package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.MacAddress;

/**
 * Centralized management traffic-group that may float among {@link Device}s within a {@link
 * DeviceGroup}.
 */
public final class TrafficGroup implements Serializable {

  public static final class Builder {

    public @Nonnull TrafficGroup build() {
      checkArgument(_name != null, "Missing %s", PROP_NAME);
      return new TrafficGroup(_haGroup, _mac, _name, _unitId);
    }

    public @Nonnull Builder setHaGroup(@Nullable String haGroup) {
      _haGroup = haGroup;
      return this;
    }

    public @Nonnull Builder setMac(@Nullable MacAddress mac) {
      _mac = mac;
      return this;
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setUnitId(@Nullable Integer unitId) {
      _unitId = unitId;
      return this;
    }

    private @Nullable String _haGroup;
    private @Nullable MacAddress _mac;
    private @Nullable String _name;
    private @Nullable Integer _unitId;

    private Builder() {}
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonProperty(PROP_HA_GROUP)
  public @Nullable String getHaGroup() {
    return _haGroup;
  }

  @JsonProperty(PROP_MAC)
  public @Nullable MacAddress getMac() {
    return _mac;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_UNIT_ID)
  public @Nullable Integer getUnitId() {
    return _unitId;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TrafficGroup)) {
      return false;
    }
    TrafficGroup rhs = (TrafficGroup) obj;
    return Objects.equals(_haGroup, rhs._haGroup)
        && Objects.equals(_mac, rhs._mac)
        && _name.equals(rhs._name)
        && Objects.equals(_unitId, rhs._unitId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_haGroup, _mac, _name, _unitId);
  }

  private static final String PROP_HA_GROUP = "haGroup";
  private static final String PROP_MAC = "mac";
  private static final String PROP_NAME = "name";
  private static final String PROP_UNIT_ID = "unitId";

  @JsonCreator
  private static @Nonnull TrafficGroup create(
      @JsonProperty(PROP_HA_GROUP) @Nullable String haGroup,
      @JsonProperty(PROP_MAC) @Nullable MacAddress mac,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_UNIT_ID) @Nullable Integer unitId) {
    Builder builder = builder().setHaGroup(haGroup).setMac(mac);
    ofNullable(name).ifPresent(builder::setName);
    return builder.setUnitId(unitId).build();
  }

  private final @Nullable String _haGroup;
  private final @Nullable MacAddress _mac;
  private final @Nonnull String _name;
  private final @Nullable Integer _unitId;

  private TrafficGroup(
      @Nullable String haGroup, @Nullable MacAddress mac, String name, @Nullable Integer unitId) {
    _haGroup = haGroup;
    _mac = mac;
    _name = name;
    _unitId = unitId;
  }
}
