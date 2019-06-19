package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class F5BigipFamily implements Serializable {

  public static final class Builder {
    private @Nonnull Map<String, Pool> _pools;
    private @Nonnull Map<String, VirtualAddress> _virtualAddresses;
    private @Nonnull Map<String, Virtual> _virtuals;

    private Builder() {
      _pools = ImmutableMap.of();
      _virtuals = ImmutableMap.of();
      _virtualAddresses = ImmutableMap.of();
    }

    public @Nonnull F5BigipFamily build() {
      return new F5BigipFamily(_pools, _virtuals, _virtualAddresses);
    }

    public @Nonnull Builder setPools(Map<String, Pool> pools) {
      _pools = ImmutableMap.copyOf(pools);
      return this;
    }

    public @Nonnull Builder setVirtualAddresses(Map<String, VirtualAddress> virtualAddresses) {
      _virtualAddresses = ImmutableMap.copyOf(virtualAddresses);
      return this;
    }

    public @Nonnull Builder setVirtuals(Map<String, Virtual> virtuals) {
      _virtuals = ImmutableMap.copyOf(virtuals);
      return this;
    }
  }

  private static final String PROP_POOLS = "pools";
  private static final String PROP_VIRTUAL_ADDRESSES = "virtualAddresses";
  private static final String PROP_VIRTUALS = "virtuals";

  private static final long serialVersionUID = 1L;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static final @Nonnull F5BigipFamily create(
      @JsonProperty(PROP_POOLS) @Nullable Map<String, Pool> pools,
      @JsonProperty(PROP_VIRTUALS) @Nullable Map<String, Virtual> virtuals,
      @JsonProperty(PROP_VIRTUAL_ADDRESSES) @Nullable
          Map<String, VirtualAddress> virtualAddresses) {
    return new F5BigipFamily(
        ImmutableMap.copyOf(firstNonNull(pools, ImmutableMap.of())),
        ImmutableMap.copyOf(firstNonNull(virtuals, ImmutableMap.of())),
        ImmutableMap.copyOf(firstNonNull(virtualAddresses, ImmutableMap.of())));
  }

  private final @Nonnull Map<String, Pool> _pools;
  private final @Nonnull Map<String, VirtualAddress> _virtualAddresses;
  private final @Nonnull Map<String, Virtual> _virtuals;

  private F5BigipFamily(
      Map<String, Pool> pools,
      Map<String, Virtual> virtuals,
      Map<String, VirtualAddress> virtualAddresses) {
    _pools = pools;
    _virtuals = virtuals;
    _virtualAddresses = virtualAddresses;
  }

  @JsonProperty(PROP_POOLS)
  public @Nonnull Map<String, Pool> getPools() {
    return _pools;
  }

  @JsonProperty(PROP_VIRTUAL_ADDRESSES)
  public @Nonnull Map<String, VirtualAddress> getVirtualAddresses() {
    return _virtualAddresses;
  }

  @JsonProperty(PROP_VIRTUALS)
  public @Nonnull Map<String, Virtual> getVirtuals() {
    return _virtuals;
  }
}
