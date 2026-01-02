package org.batfish.datamodel.vendor_family.cumulus;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.Objects;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Cumulus-specific configuration information exposed in data model */
public final class CumulusFamily implements Serializable {

  public static final class Builder {

    private @Nullable Bridge _bridge;
    private @Nonnull SortedMap<String, InterfaceClagSettings> _interfaceClagSettings;

    private Builder() {
      _interfaceClagSettings = ImmutableSortedMap.of();
    }

    public @Nonnull CumulusFamily build() {
      checkArgument(_bridge != null, "Missing %s", PROP_BRIDGE);
      return new CumulusFamily(_bridge, _interfaceClagSettings);
    }

    public @Nonnull Builder setBridge(Bridge bridge) {
      _bridge = bridge;
      return this;
    }

    public @Nonnull Builder setInterfaceClagSettings(
        SortedMap<String, InterfaceClagSettings> interfaceClagSettings) {
      _interfaceClagSettings = interfaceClagSettings;
      return this;
    }
  }

  private static final String PROP_BRIDGE = "bridge";
  private static final String PROP_INTERFACE_CLAG_SETTINGS = "interfaceClagSettings";

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static final @Nonnull CumulusFamily create(
      @JsonProperty(PROP_BRIDGE) @Nullable Bridge bridge,
      @JsonProperty(PROP_INTERFACE_CLAG_SETTINGS) @Nullable
          SortedMap<String, InterfaceClagSettings> interfaceClagSettings) {
    checkArgument(bridge != null, "Missing %s", PROP_BRIDGE);
    return new CumulusFamily(
        bridge,
        ImmutableSortedMap.copyOf(firstNonNull(interfaceClagSettings, ImmutableSortedMap.of())));
  }

  private final @Nonnull Bridge _bridge;
  private final @Nonnull SortedMap<String, InterfaceClagSettings> _interfaceClagSettings;

  private CumulusFamily(
      Bridge bridge, SortedMap<String, InterfaceClagSettings> interfaceClagSettings) {
    _bridge = bridge;
    _interfaceClagSettings = interfaceClagSettings;
  }

  @JsonProperty(PROP_BRIDGE)
  public @Nonnull Bridge getBridge() {
    return _bridge;
  }

  @JsonProperty(PROP_INTERFACE_CLAG_SETTINGS)
  public @Nonnull SortedMap<String, InterfaceClagSettings> getInterfaceClagSettings() {
    return _interfaceClagSettings;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CumulusFamily)) {
      return false;
    }
    CumulusFamily rhs = (CumulusFamily) obj;
    return _bridge.equals(rhs._bridge) && _interfaceClagSettings.equals(rhs._interfaceClagSettings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_bridge, _interfaceClagSettings);
  }

  @Override
  public @Nonnull String toString() {
    return toStringHelper(getClass())
        .add(PROP_BRIDGE, _bridge)
        .add(PROP_INTERFACE_CLAG_SETTINGS, _interfaceClagSettings)
        .toString();
  }
}
