package org.batfish.datamodel.vendor_family.cumulus;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Cumulus-specific configuration information exposed in data model */
public final class CumulusFamily implements Serializable {

  public static final class Builder {

    private @Nonnull SortedMap<String, InterfaceClagSettings> _interfaceClagSettings;

    private Builder() {
      _interfaceClagSettings = ImmutableSortedMap.of();
    }

    public @Nonnull CumulusFamily build() {
      return new CumulusFamily(_interfaceClagSettings);
    }

    public @Nonnull Builder setInterfaceClagSettings(
        SortedMap<String, InterfaceClagSettings> interfaceClagSettings) {
      _interfaceClagSettings = interfaceClagSettings;
      return this;
    }
  }

  private static final String PROP_INTERFACE_CLAG_SETTINGS = "interfaceClagSettings";

  private static final long serialVersionUID = 1L;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static final @Nonnull CumulusFamily create(
      @JsonProperty(PROP_INTERFACE_CLAG_SETTINGS) @Nullable
          SortedMap<String, InterfaceClagSettings> interfaceClagSettings) {
    return new CumulusFamily(
        ImmutableSortedMap.copyOf(firstNonNull(interfaceClagSettings, ImmutableSortedMap.of())));
  }

  private final @Nonnull SortedMap<String, InterfaceClagSettings> _interfaceClagSettings;

  private CumulusFamily(SortedMap<String, InterfaceClagSettings> interfaceClagSettings) {
    _interfaceClagSettings = interfaceClagSettings;
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
    return _interfaceClagSettings.equals(rhs._interfaceClagSettings);
  }

  @Override
  public int hashCode() {
    return _interfaceClagSettings.hashCode();
  }

  @Override
  public @Nonnull String toString() {
    return toStringHelper(getClass())
        .add(PROP_INTERFACE_CLAG_SETTINGS, _interfaceClagSettings)
        .toString();
  }
}
